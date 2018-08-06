package com.walmartlabs.ticketservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.walmartlabs.ticketservice.entity.SeatHold;
import com.walmartlabs.ticketservice.entity.VenueSection;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Ticket service integration test where real APIs and real methods are called.
 *
 * Normally, integration/automation test can run as a standalone application.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TicketServiceIT {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  /*
   * The application creates schema and loads resources/data.sql at startup.
   * numSeatsAvailable (the sum of availableSeats in all venue sections) = 550.
   */
  @Test
  public void integrationTest() {
    /*
     * Make call to get numSeatsAvailable.
     * Expect numSeatsAvailable = 550.
     */
    ResponseEntity<Map> responseEntity1 = restTemplate.getForEntity(String.format(
        "http://localhost:%s/v1/numSeatsAvailable", port), Map.class);

    assertEquals(200, responseEntity1.getStatusCodeValue());
    assertNotNull(responseEntity1.getBody());
    assertEquals(550, (int) responseEntity1.getBody().get("numSeatsAvailable"));

    /*
     * Make call to find and hold 1000 seats.
     * Expect all of 550 seats to be held as 1000 > 550.
     */
    ResponseEntity<SeatHold> responseEntity2 = restTemplate.postForEntity(String.format(
        "http://localhost:%s/v1/findAndHoldSeats/%s/%s", port, 1000, "test@test.com"),
        null, SeatHold.class);

    assertEquals(200, responseEntity2.getStatusCodeValue());
    assertNotNull(responseEntity2.getBody());
    assertEquals(550, responseEntity2.getBody().getNumSeats());

    /*
     * Make call to get numSeatsAvailable again.
     * Expect numSeatsAvailable = 0 as all seats have been held.
     */
    ResponseEntity<Map> responseEntity3 = restTemplate.getForEntity(String.format(
        "http://localhost:%s/v1/numSeatsAvailable", port), Map.class);

    assertEquals(200, responseEntity3.getStatusCodeValue());
    assertNotNull(responseEntity3.getBody());
    assertEquals(0, (int) responseEntity3.getBody().get("numSeatsAvailable"));

    /*
     * Make call to find and hold 10 seats.
     * Expect no seat to be held as numSeatsAvailable = 0.
     */
    ResponseEntity<SeatHold> responseEntity4 = restTemplate.postForEntity(String.format(
        "http://localhost:%s/v1/findAndHoldSeats/%s/%s", port, 1000, "test@test.com"),
        null, SeatHold.class);

    assertEquals(200, responseEntity4.getStatusCodeValue());
    assertNotNull(responseEntity4.getBody());
    assertEquals(0, responseEntity4.getBody().getNumSeats());

    /*
     * Make call to reserve seats for responseEntity2 with a different customer email.
     * Expect 400 BAD REQUEST.
     */
    ResponseEntity<Map> responseEntity5 = restTemplate.postForEntity(String.format(
        "http://localhost:%s/v1/reserveSeats/%s/%s",
        port, responseEntity2.getBody().getId(), "different@different.com"), null, Map.class);

    assertEquals(400, responseEntity5.getStatusCodeValue());
    assertNotNull(responseEntity5.getBody());
    assertNotNull(responseEntity5.getBody().get("message"));

    /*
     * Make call to reserve seats for responseEntity2.
     * Expect a confirmation code to be returned.
     */
    ResponseEntity<Map> responseEntity6 = restTemplate.postForEntity(String.format(
        "http://localhost:%s/v1/reserveSeats/%s/%s",
        port, responseEntity2.getBody().getId(), "test@test.com"), null, Map.class);

    assertEquals(200, responseEntity6.getStatusCodeValue());
    assertNotNull(responseEntity6.getBody());
    assertNotNull(responseEntity6.getBody().get("confirmationCode"));

    /*
     * Make call to reserve seats for a SeatHold that does not exit.
     * Expect 400 BAD REQUEST.
     */
    ResponseEntity<Map> responseEntity7 = restTemplate.postForEntity(String.format(
        "http://localhost:%s/v1/reserveSeats/%s/%s", port, 2333, "test@test.com"), null, Map.class);

    assertEquals(400, responseEntity7.getStatusCodeValue());
    assertNotNull(responseEntity7.getBody());
    assertNotNull(responseEntity7.getBody().get("message"));

    /*
     * Make call to reset the venue.
     * Expected availableSeats = totalSeats for every VenueSection.
     */
    ResponseEntity<List> responseEntity8 = restTemplate.postForEntity(String.format(
        "http://localhost:%s/admin/resetVenue", port), null, List.class);
    List<VenueSection> venueSections = responseEntity8.getBody();

    assertEquals(200, responseEntity8.getStatusCodeValue());
    assertNotNull(venueSections);
    assertEquals(10, venueSections.size());

    /*
     * Make call to get numSeatsAvailable after venue is reset.
     * Expect numSeatsAvailable = 550.
     */
    ResponseEntity<Map> responseEntity9 = restTemplate.getForEntity(String.format(
        "http://localhost:%s/v1/numSeatsAvailable", port), Map.class);

    assertEquals(200, responseEntity9.getStatusCodeValue());
    assertNotNull(responseEntity9.getBody());
    assertEquals(550, (int) responseEntity9.getBody().get("numSeatsAvailable"));
  }
}
