package com.walmartlabs.ticketservice.service;

import com.walmartlabs.ticketservice.entity.SeatHold;
import com.walmartlabs.ticketservice.entity.VenueSection;
import com.walmartlabs.ticketservice.repository.SeatHoldRepository;
import com.walmartlabs.ticketservice.repository.VenueSectionRepository;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * RecycleService is scheduled to run every ${fixedRate} seconds to clean up expired SeatHold
 * records.
 *
 * It is better to make RecycleService a standalone application/micro-service, especially for
 * distributed systems.
 **/
@Slf4j
@Service
public class RecycleService {

  @Value("${seatHold.expiration}")
  private long expiration;

  @Autowired
  private SeatHoldRepository seatHoldRepository;
  @Autowired
  private VenueSectionRepository venueSectionRepository;

  @Scheduled(fixedRateString = "${recycle.fixedRate}")
  public void recycle() {
    log.info("Scheduled job starts to recycle expired SeatHold records.");

    seatHoldRepository.findAllByCreatedAtBefore(new Date(new Date().getTime() - expiration))
        .forEach(this::recycle);
  }

  // Recycle the seats to VenueSection and delete the SeatHold from database.
  public void recycle(SeatHold seatHold) {
    seatHold.getSeatDetails().forEach(seatDetail -> {
      VenueSection venueSection = seatDetail.getVenueSection();

      // Update availableSeats in VenueSection.
      venueSection.setAvailableSeats(venueSection.getAvailableSeats()
          + seatDetail.getNumSeats());
      venueSectionRepository.save(venueSection);

      log.info(String.format("Recycled %s seat(s) to VenueSection with id %s.",
          seatDetail.getNumSeats(), seatDetail.getVenueSection().getId()));
    });

    seatHoldRepository.delete(seatHold);

    log.info(String.format("Deleted SeatHold with id %s, recycled %s seat(s) in total.",
        seatHold.getId(), seatHold.getNumSeats()));
  }
}
