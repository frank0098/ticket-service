package com.walmartlabs.ticketservice.service;

import com.walmartlabs.ticketservice.entity.SeatHold;

public interface TicketService {

  /**
   * The number of seatDetails in the venue that are neither held nor reserved
   *
   * @return the number of tickets available in the venue
   */
  int numSeatsAvailable();

  /**
   * Find and hold the best available seatDetails for a customer
   *
   * @param numSeats the number of seatDetails to find and hold
   * @param customerEmail unique identifier for the customer
   * @return a SeatHold object identifying the specific seatDetails and related information
   */
  SeatHold findAndHoldSeats(int numSeats, String customerEmail);

  /**
   * Commit seatDetails held for a specific customer
   *
   * @param seatHoldId the seat hold identifier
   * @param customerEmail the email address of the customer to which the seat hold is assigned
   * @return a reservation confirmation code
   */
  String reserveSeats(int seatHoldId, String customerEmail);
}