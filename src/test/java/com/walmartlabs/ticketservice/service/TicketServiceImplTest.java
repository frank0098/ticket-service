package com.walmartlabs.ticketservice.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.walmartlabs.ticketservice.entity.Customer;
import com.walmartlabs.ticketservice.entity.Reservation;
import com.walmartlabs.ticketservice.entity.SeatHold;
import com.walmartlabs.ticketservice.entity.VenueSection;
import com.walmartlabs.ticketservice.exception.TicketServiceException;
import com.walmartlabs.ticketservice.repository.CustomerRepository;
import com.walmartlabs.ticketservice.repository.ReservationRepository;
import com.walmartlabs.ticketservice.repository.SeatHoldRepository;
import com.walmartlabs.ticketservice.repository.VenueSectionRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {

  @InjectMocks
  private TicketServiceImpl ticketService;
  @Mock
  private CustomerRepository customerRepository;
  @Mock
  private ReservationRepository reservationRepository;
  @Mock
  private SeatHoldRepository seatHoldRepository;
  @Mock
  private VenueSectionRepository venueSectionRepository;
  @Mock
  private RecycleService recycleService;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(ticketService, "expiration", 3000);
  }

  // Test numSeatsAvailable method happy path.
  @Test
  public void testNumSeatsAvailable() {
    List<VenueSection> venueSections = new ArrayList<>();
    venueSections.add(VenueSection.builder().id(1).availableSeats(100).build());
    venueSections.add(VenueSection.builder().id(2).availableSeats(200).build());
    when(venueSectionRepository.findAllByAvailableSeatsGreaterThan(0)).thenReturn(venueSections);

    assertEquals(300, ticketService.numSeatsAvailable());
  }

  // Test findAndHoldSeats method when all seats to be held lie in same VenueSection.
  @Test
  public void testFindAndHoldSeats_SameVenueSection() {
    Customer customer = Customer.builder().id(1).email("test@test.com").build();
    when(customerRepository.findByEmail("test@test.com")).thenReturn(Optional.of(customer));

    VenueSection venueSection = VenueSection.builder().id(2).availableSeats(100).build();
    when(venueSectionRepository.findFirstByAvailableSeatsGreaterThanEqual(anyInt()))
        .thenReturn(Optional.of(venueSection));
    when(venueSectionRepository.save(any(VenueSection.class))).thenReturn(venueSection);

    SeatHold seatHold = SeatHold.builder().id(3).customer(customer).numSeats(10).build();
    when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(seatHold);

    assertEquals(3, ticketService.findAndHoldSeats(10, "test@test.com").getId());
    assertEquals(1, ticketService.findAndHoldSeats(10, "test@test.com").getCustomer().getId());
    assertEquals(10, ticketService.findAndHoldSeats(10, "test@test.com").getNumSeats());
  }

  // Test findAndHoldSeats method when seats to be held lie in multiple venue sections.
  @Test
  public void testFindAndHoldSeats_MultipleVenueSections() {
    Customer customer = Customer.builder().id(1).email("test@test.com").build();
    when(customerRepository.save(any(Customer.class))).thenReturn(customer);
    when(venueSectionRepository.findFirstByAvailableSeatsGreaterThanEqual(anyInt()))
        .thenReturn(Optional.empty());

    List<VenueSection> venueSections = new ArrayList<>();
    venueSections.add(VenueSection.builder().id(1).availableSeats(100).build());
    venueSections.add(VenueSection.builder().id(2).availableSeats(200).build());
    venueSections.add(VenueSection.builder().id(3).availableSeats(300).build());
    when(venueSectionRepository.findAllByAvailableSeatsGreaterThan(0)).thenReturn(venueSections);
    when(venueSectionRepository.save(any(VenueSection.class)))
        .thenReturn(VenueSection.builder().build());

    SeatHold seatHold = SeatHold.builder().id(4).customer(customer).numSeats(500).build();
    when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(seatHold);

    assertEquals(4, ticketService.findAndHoldSeats(10, "test@test.com").getId());
    assertEquals(1, ticketService.findAndHoldSeats(10, "test@test.com").getCustomer().getId());
    assertEquals(500, ticketService.findAndHoldSeats(500, "test@test.com").getNumSeats());
  }

  // Test reserveSeats happy path.
  @Test
  public void testReserveSeats() {
    Customer customer = Customer.builder().id(1).email("test@test.com").build();
    SeatHold seatHold = SeatHold.builder().id(2).customer(customer).seatDetails(new ArrayList<>())
        .createdAt(new Date()).build();
    when(seatHoldRepository.findById(2)).thenReturn(Optional.of(seatHold));

    Reservation reservation = Reservation.builder().id("test-code").build();
    when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

    assertEquals("test-code", ticketService.reserveSeats(2, "test@test.com"));
  }

  // Test reserveSeats when SeatHold with the specific seatHoldId does not exist.
  @Test(expected = TicketServiceException.class)
  public void testReserveSeats_SeatHoldDoesNotExist() {
    when(seatHoldRepository.findById(2)).thenReturn(Optional.empty());
    ticketService.reserveSeats(2, "test@test.com");
  }

  // Test reserveSeats when customerEmail is different from SeatHold.customer.email.
  @Test(expected = TicketServiceException.class)
  public void testReserveSeats_UnAuthorized() {
    Customer customer = Customer.builder().id(1).email("test@test.com").build();
    SeatHold seatHold = SeatHold.builder().id(2).customer(customer).createdAt(new Date()).build();
    when(seatHoldRepository.findById(2)).thenReturn(Optional.of(seatHold));

    ticketService.reserveSeats(2, "abc@test.com");
  }

  // Test reserveSeats when SeatHold with the specific seatHoldId is expired
  @Test(expected = TicketServiceException.class)
  public void testReserveSeats_SeatHoldIsExpired() throws InterruptedException {
    Customer customer = Customer.builder().id(1).email("test@test.com").build();
    SeatHold seatHold = SeatHold.builder().id(2).customer(customer).createdAt(new Date()).build();
    when(seatHoldRepository.findById(2)).thenReturn(Optional.of(seatHold));

    Thread.sleep(3001);

    doNothing().when(recycleService).recycle(seatHold);
    ticketService.reserveSeats(2, "test@test.com");
  }
}
