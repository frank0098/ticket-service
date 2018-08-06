package com.walmartlabs.ticketservice.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

/**
 * VenueSection info and the number of seats held/reserved in this VenueSection.
 */
@Data
@Entity
@Builder
public class SeatDetail {

  @Id
  @GeneratedValue
  private int id;

  @ManyToOne
  private VenueSection venueSection;

  // The number of seats held/reserved in this VenueSection.
  private int numSeats;
}
