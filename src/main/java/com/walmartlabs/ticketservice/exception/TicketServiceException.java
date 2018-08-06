package com.walmartlabs.ticketservice.exception;

import lombok.NoArgsConstructor;

/**
 * A generic exception class which is enough for a small application.
 *
 * If the application gets bigger, we need to create more specific classes to handle different types
 * of exceptions, e.g., CustomerException (customer is unauthorized, etc.), SeatHoldException
 * (SeatHold does not exist or is expired, etc.).
 **/
@NoArgsConstructor
public class TicketServiceException extends RuntimeException {

  public TicketServiceException(String message) {
    super(message);
  }
}
