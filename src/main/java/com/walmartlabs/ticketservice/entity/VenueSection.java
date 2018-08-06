package com.walmartlabs.ticketservice.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

/**
 * The venue is divided into multiple sections. It is best to hold numSeats of seats in same
 * section. But if that is not possible, the seats will be held in multiple sections.
 */
@Data
@Entity
@Builder
@DynamicUpdate
public class VenueSection {

  @Id
  @GeneratedValue
  private int id;

  private int totalSeats;

  private int availableSeats;
}
