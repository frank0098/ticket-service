package com.walmartlabs.ticketservice.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.walmartlabs.ticketservice.entity.SeatHold;
import com.walmartlabs.ticketservice.exception.TicketServiceException;
import com.walmartlabs.ticketservice.service.TicketServiceImpl;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceControllerTest {

  @InjectMocks
  private TicketServiceController controller;
  @Mock
  private TicketServiceImpl ticketService;

  // Test numSeatsAvailable method happy path.
  @Test
  public void testNumSeatsAvailable() {
    when(ticketService.numSeatsAvailable()).thenReturn(100);

    ResponseEntity<Map<String, Integer>> responseEntity = controller.numSeatsAvailable();

    assertEquals(200, responseEntity.getStatusCodeValue());
    assertNotNull(responseEntity.getBody());
    assertEquals(100, (int) responseEntity.getBody().get("numSeatsAvailable"));
  }

  // Test findAndHoldSeats method happy path.
  @Test
  public void testFindAndHoldSeats() {
    SeatHold seatHold = SeatHold.builder().id(2333).build();
    when(ticketService.findAndHoldSeats(anyInt(), anyString())).thenReturn(seatHold);

    ResponseEntity<SeatHold> responseEntity = controller.findAndHoldSeats(100, "test@test.com");

    assertEquals(200, responseEntity.getStatusCodeValue());
    assertNotNull(responseEntity.getBody());
    assertEquals(2333, responseEntity.getBody().getId());
  }

  // Test reserveSeats method happy path.
  @Test
  public void testReserveSeats() {
    when(ticketService.reserveSeats(anyInt(), anyString())).thenReturn("test-code");

    ResponseEntity<Map<String, String>> responseEntity = controller
        .reserveSeats(2333, "test@test.com");

    assertEquals(200, responseEntity.getStatusCodeValue());
    assertNotNull(responseEntity.getBody());
    assertEquals("test-code", responseEntity.getBody().get("confirmationCode"));
  }

  /*
   * Test reserveSeats method when ticketService throws TicketServiceException.
   * The method is expected to return 400 BAD REQUEST with error message.
   */
  @Test
  public void testReserveSeats_BadRequest() {
    when(ticketService.reserveSeats(anyInt(), anyString()))
        .thenThrow(new TicketServiceException("test-message"));

    ResponseEntity<Map<String, String>> responseEntity = controller
        .reserveSeats(2333, "test@test.com");

    assertEquals(400, responseEntity.getStatusCodeValue());
    assertNotNull(responseEntity.getBody());
    assertEquals("test-message", responseEntity.getBody().get("message"));
  }
}
