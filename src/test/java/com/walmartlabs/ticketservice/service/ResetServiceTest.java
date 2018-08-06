package com.walmartlabs.ticketservice.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.walmartlabs.ticketservice.entity.VenueSection;
import com.walmartlabs.ticketservice.repository.SeatHoldRepository;
import com.walmartlabs.ticketservice.repository.VenueSectionRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResetServiceTest {

  @InjectMocks
  private ResetService resetService;
  @Mock
  private SeatHoldRepository seatHoldRepository;
  @Mock
  private VenueSectionRepository venueSectionRepository;

  // Test resetVenue method happy path.
  @Test
  public void testResetVenue() {
    List<VenueSection> venueSections = Arrays.asList(
        VenueSection.builder().id(1).availableSeats(0).totalSeats(100).build(),
        VenueSection.builder().id(2).availableSeats(100).totalSeats(500).build()
    );
    doNothing().when(seatHoldRepository).deleteAll();
    when(venueSectionRepository.findAll()).thenReturn(venueSections);

    List<VenueSection> result = resetService.resetVenue();

    assertEquals(2, result.size());
    assertEquals(100, result.get(0).getAvailableSeats());
    assertEquals(500, result.get(1).getAvailableSeats());
  }
}
