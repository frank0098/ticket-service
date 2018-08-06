package com.walmartlabs.ticketservice.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Once a SeatHold is expired or reserved, it will be deleted from database.
 **/
@Data
@Entity
@Builder
public class SeatHold {

  @Id
  @GeneratedValue
  private int id;

  @ManyToOne
  private Customer customer;

  // The number of held seats.
  private int numSeats;

  // The held seats can lie in one or multiple venue sections.
  @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
  @JoinColumn(name = "seat_hold_id")
  private List<SeatDetail> seatDetails;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;
}
