package com.walmartlabs.ticketservice.service;

import com.walmartlabs.ticketservice.entity.Customer;
import com.walmartlabs.ticketservice.entity.Reservation;
import com.walmartlabs.ticketservice.entity.SeatDetail;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TicketServiceImpl implements TicketService {

  @Value("${seatHold.expiration}")
  private long expiration;

  @Autowired
  private CustomerRepository customerRepository;
  @Autowired
  private ReservationRepository reservationRepository;
  @Autowired
  private SeatHoldRepository seatHoldRepository;
  @Autowired
  private VenueSectionRepository venueSectionRepository;

  @Autowired
  private RecycleService recycleService;

  @Override
  public int numSeatsAvailable() {
    log.info("Getting the number of seats available in the venue.");

    List<VenueSection> venueSections = venueSectionRepository
        .findAllByAvailableSeatsGreaterThan(0);

    // Sum availableSeats in all venue sections.
    int numSeatsAvailable = venueSections.stream()
        .mapToInt(VenueSection::getAvailableSeats)
        .sum();

    log.info(String.format("The number of seats available in the venue is %s.", numSeatsAvailable));
    return numSeatsAvailable;
  }

  @Override
  public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
    log.info(String.format("Finding and holding %s seats for customer with email %s",
        numSeats, customerEmail));

    // Retrieve Customer from database, or create a new one if the Customer does not exist.
    Optional<Customer> customerOptional = customerRepository.findByEmail(customerEmail);
    Customer customer;

    if (customerOptional.isPresent()) {
      customer = customerOptional.get();
    } else {
      log.info(String.format("Creating new Customer with email %s", customerEmail));
      customer = customerRepository.save(Customer.builder().email(customerEmail).build());
    }

    SeatHold seatHold;

    // Find if numSeats of seats can lie in same VenueSection.
    Optional<VenueSection> venueSectionOptional = venueSectionRepository
        .findFirstByAvailableSeatsGreaterThanEqual(numSeats);

    if (venueSectionOptional.isPresent()) {
      /*
       * numSeats of seats lie in SAME VenueSection.
       * Hold the seats in the VenueSection.
       */

      VenueSection venueSection = venueSectionOptional.get();
      List<SeatDetail> seatDetails = new ArrayList<>();

      seatDetails.add(SeatDetail.builder()
          .venueSection(venueSection)
          .numSeats(numSeats)
          .build());

      // Update availableSeats in the VenueSection.
      venueSection.setAvailableSeats(venueSection.getAvailableSeats() - numSeats);
      venueSectionRepository.save(venueSection);

      // Write SeatHold into database.
      seatHold = seatHoldRepository.save(SeatHold.builder()
          .customer(customer)
          .numSeats(numSeats)
          .seatDetails(seatDetails)
          .build());
    } else {
      /*
       * numSeats of seats lie in MULTIPLE venue sections.
       * Hold the seats in every VenueSection.
       */

      List<VenueSection> venueSections = venueSectionRepository
          .findAllByAvailableSeatsGreaterThan(0);
      List<SeatDetail> seatDetails = new ArrayList<>();

      // Current number of held seats.
      int count = 0;

      for (VenueSection section : venueSections) {
        if (section.getAvailableSeats() >= numSeats - count) {
          seatDetails.add(SeatDetail.builder()
              .venueSection(section)
              .numSeats(numSeats - count)
              .build());

          // Update availableSeats in the VenueSection.
          section.setAvailableSeats(section.getAvailableSeats() - numSeats + count);
          venueSectionRepository.save(section);

          // All numSeats of seats have been held.
          count = numSeats;
          break;
        } else {
          seatDetails.add(SeatDetail.builder()
              .venueSection(section)
              .numSeats(section.getAvailableSeats())
              .build());

          // Hold as many seats as possible.
          count += section.getAvailableSeats();

          // Update availableSeats in the VenueSection to 0.
          section.setAvailableSeats(0);
          venueSectionRepository.save(section);
        }
      }

      // Write SeatHold into database.
      seatHold = seatHoldRepository.save(SeatHold.builder()
          .customer(customer)
          .numSeats(count)
          .seatDetails(seatDetails)
          .build());
    }

    log.info(String.format("Successfully found and held %s seats for customer with email %s",
        seatHold.getNumSeats(), customerEmail));

    return seatHold;
  }

  @Override
  public String reserveSeats(int seatHoldId, String customerEmail) {
    log.info(String.format("Reserving seats for SeatHold with id %s and customer with email %s.",
        seatHoldId, customerEmail));

    Optional<SeatHold> seatHoldOptional = seatHoldRepository.findById(seatHoldId);

    if (seatHoldOptional.isPresent()) {
      SeatHold seatHold = seatHoldOptional.get();

      /*
       * We need to ensure the customer can only access his/her own seatHold records. Normally, we
       * do this using @PreAuthorize(customerEmail == authentication.email) in Spring Security. But
       * as we are not using Spring Security here, we add a simple check below.
       */
      if (!seatHold.getCustomer().getEmail().equalsIgnoreCase(customerEmail)) {
        String message = String.format("The customer with email %s is not authorized to access "
            + "SeatHold with id %s.", customerEmail, seatHoldId);

        log.error(message);
        throw new TicketServiceException(message);
      }

      //The SeatHold may expire in between last recycle job and next scheduled recycle job.
      if (new Date().getTime() - seatHold.getCreatedAt().getTime() > expiration) {
        recycleService.recycle(seatHold);
        String message = String.format("The SeatHold with id %s is expired.", seatHoldId);

        log.warn(message);
        throw new TicketServiceException(message);
      } else {
        // Write Reservation into database.
        String confirmationCode = reservationRepository.save(Reservation.builder()
            .customer(seatHold.getCustomer())
            .numSeats(seatHold.getNumSeats())
            .seatDetails(new ArrayList<>(seatHold.getSeatDetails()))
            .build())
            .getId();

        //The seats are reserved, so the SeatHold can be deleted from database.
        seatHoldRepository.delete(seatHold);

        log.info(String.format("Successfully reserved seats for SeatHold with id %s and customer "
            + "with email %s.", seatHoldId, customerEmail));

        return confirmationCode;
      }
    } else {
      String message = String.format("The SeatHold with id %s does not exist or is expired.",
          seatHoldId);

      log.error(message);
      throw new TicketServiceException(message);
    }
  }
}
