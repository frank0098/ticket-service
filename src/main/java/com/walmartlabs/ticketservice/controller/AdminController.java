package com.walmartlabs.ticketservice.controller;

import com.walmartlabs.ticketservice.entity.VenueSection;
import com.walmartlabs.ticketservice.service.ResetService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

  @Autowired
  private ResetService resetService;

  /**
   * Reset Venue and mark all seats available. This will delete all SeatHold data and set
   * availableSeats to totalSeats for every VenueSection.
   *
   * @return 200 OK with the list of reset VenueSection.
   */
  @PostMapping("/resetVenue")
  public ResponseEntity<List<VenueSection>> resetVenue() {
    return ResponseEntity.ok(resetService.resetVenue());
  }
}
