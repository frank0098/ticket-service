package com.walmartlabs.ticketservice.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import com.walmartlabs.ticketservice.entity.VenueSection;
import com.walmartlabs.ticketservice.service.ResetService;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {

  @InjectMocks
  private AdminController adminController;
  @Mock
  private ResetService resetService;

  // Test resetVenue method happy path.
  @Test
  public void testResetVenue() {
    List<VenueSection> venueSections = Arrays.asList(
        VenueSection.builder().id(1).build(),
        VenueSection.builder().id(2).build()
    );
    when(resetService.resetVenue()).thenReturn(venueSections);

    ResponseEntity responseEntity = adminController.resetVenue();
    List<VenueSection> response = (List<VenueSection>) responseEntity.getBody();

    assertEquals(200, responseEntity.getStatusCodeValue());
    assertNotNull(response);
    assertEquals(2, response.size());
  }
}
