package com.walmartlabs.ticketservice.service;

import com.walmartlabs.ticketservice.entity.VenueSection;
import com.walmartlabs.ticketservice.repository.SeatHoldRepository;
import com.walmartlabs.ticketservice.repository.VenueSectionRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResetService {

  @Autowired
  private SeatHoldRepository seatHoldRepository;
  @Autowired
  private VenueSectionRepository venueSectionRepository;

  /*
   * Delete all SeatHold data and set availableSeats to totalSeats for every VenueSection.
   * This won't affect other tables.
   *
   * @return the list of reset VenueSection.
   */
  public List<VenueSection> resetVenue() {
    log.info("Resetting venue. All SeatHold data will be deleted and availableSeats will be set to "
        + "totalSeats for every VenueSection.");

    seatHoldRepository.deleteAll();
    List<VenueSection> venueSections = venueSectionRepository.findAll();

    venueSections.forEach(venueSection -> {
      // Set availableSeats to totalSeats.
      venueSection.setAvailableSeats(venueSection.getTotalSeats());
      venueSectionRepository.save(venueSection);
    });

    log.info("Successfully reset venue.");
    return venueSections;
  }
}
