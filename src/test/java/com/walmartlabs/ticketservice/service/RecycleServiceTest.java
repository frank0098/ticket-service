package com.walmartlabs.ticketservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.walmartlabs.ticketservice.entity.SeatDetail;
import com.walmartlabs.ticketservice.entity.SeatHold;
import com.walmartlabs.ticketservice.entity.VenueSection;
import com.walmartlabs.ticketservice.repository.SeatHoldRepository;
import com.walmartlabs.ticketservice.repository.VenueSectionRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecycleServiceTest {

  @InjectMocks
  private RecycleService recycleService;
  @Mock
  private SeatHoldRepository seatHoldRepository;
  @Mock
  private VenueSectionRepository venueSectionRepository;

  // Test recycle method happy path.
  @Test
  public void testRecycle() {
    List<SeatHold> seatHolds = new ArrayList<>();
    VenueSection venueSection = VenueSection.builder().id(1).availableSeats(1000).build();
    seatHolds.add(SeatHold.builder().id(2).numSeats(200).seatDetails(Arrays.asList(
        SeatDetail.builder().id(3).venueSection(venueSection).numSeats(100).build(),
        SeatDetail.builder().id(4).venueSection(venueSection).numSeats(100).build()
    )).build());
    seatHolds.add(SeatHold.builder().id(5).numSeats(300).seatDetails(Arrays.asList(
        SeatDetail.builder().id(6).venueSection(venueSection).numSeats(300).build()
    )).build());
    when(seatHoldRepository.findAllByCreatedAtBefore(any(Date.class))).thenReturn(seatHolds);

    when(venueSectionRepository.save(any(VenueSection.class))).thenReturn(venueSection);
    doNothing().when(seatHoldRepository).delete(any(SeatHold.class));
    recycleService.recycle();
  }
}
