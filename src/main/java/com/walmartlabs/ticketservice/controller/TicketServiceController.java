package com.walmartlabs.ticketservice.controller;

import com.walmartlabs.ticketservice.entity.SeatHold;
import com.walmartlabs.ticketservice.exception.TicketServiceException;
import com.walmartlabs.ticketservice.service.TicketServiceImpl;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ticket service rest controller v1.
 */
@RestController
@RequestMapping("/v1")
public class TicketServiceController {

  @Autowired
  private TicketServiceImpl ticketService;

  /**
   * Get the number of seats available in the venue.
   *
   * @return 200 OK with numSeatsAvailable.
   */
  @GetMapping("/numSeatsAvailable")
  public ResponseEntity<Map<String, Integer>> numSeatsAvailable() {
    Map<String, Integer> response = new HashMap<>();
    response.put("numSeatsAvailable", ticketService.numSeatsAvailable());
    return ResponseEntity.ok(response);
  }

  /**
   * Find and hold a specific number of seats for a specific Customer.
   *
   * @param numSeats the number of seats to find and hold.
   * @param customerEmail the email of the Customer.
   * @return 200 OK with SeatHold object.
   */
  @PostMapping("/findAndHoldSeats/{numSeats}/{customerEmail}")
  public ResponseEntity<SeatHold> findAndHoldSeats(@PathVariable int numSeats,
      @PathVariable String customerEmail) {
    return ResponseEntity.ok(ticketService.findAndHoldSeats(numSeats, customerEmail));
  }

  /**
   * Reserve seats for a specific SeatHold and a specific Customer.
   *
   * @param seatHoldId the SeatHold id.
   * @param customerEmail the email of the Customer
   * @return 200 OK with confirmationCode, or 400 BAD REQUEST if the SeatHold does not exist or the
   * Customer is not authorized to access the SeatHold.
   */
  @PostMapping("/reserveSeats/{seatHoldId}/{customerEmail}")
  public ResponseEntity<Map<String, String>> reserveSeats(@PathVariable int seatHoldId,
      @PathVariable String customerEmail) {
    Map<String, String> response = new HashMap<>();

    try {
      response.put("confirmationCode", ticketService.reserveSeats(seatHoldId, customerEmail));
      return ResponseEntity.ok(response);
    } catch (TicketServiceException e) {
      /*
       * Normally, we should return 403 FORBIDDEN if the customer is not authorized to access the
       * SeatHold, and we can do that using Spring Security. However, in this coding challenge,
       * we are not using Spring Security, so we just return 400 BAD REQUEST.
       */
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}
