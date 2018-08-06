package com.walmartlabs.ticketservice.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.walmartlabs.ticketservice.entity.VenueSection;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class VenueSectionRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private VenueSectionRepository venueSectionRepository;

  // Test findAll methods.
  @Test
  public void testFindAll() {
    venueSectionRepository.deleteAll();

    entityManager.persist(VenueSection.builder().build());
    entityManager.persist(VenueSection.builder().build());
    entityManager.flush();

    assertEquals(2, venueSectionRepository.findAll().size());
  }

  // Test findAllByAvailableSeatsGreaterThan method.
  @Test
  public void testFindAllByAvailableSeatsGreaterThan() {
    venueSectionRepository.deleteAll();

    entityManager.persist(VenueSection.builder().availableSeats(100).build());
    entityManager.persist(VenueSection.builder().availableSeats(200).build());
    entityManager.persist(VenueSection.builder().availableSeats(300).build());
    entityManager.flush();

    assertEquals(2, venueSectionRepository.findAllByAvailableSeatsGreaterThan(100).size());
  }

  // Test findFirstByAvailableSeatsGreaterThanEqual method.
  @Test
  public void testFindFirstByAvailableSeatsGreaterThanEqual() {
    venueSectionRepository.deleteAll();

    entityManager.persist(VenueSection.builder().availableSeats(100).build());
    entityManager.persist(VenueSection.builder().availableSeats(200).build());
    entityManager.flush();

    Optional<VenueSection> venueSectionOptional = venueSectionRepository
        .findFirstByAvailableSeatsGreaterThanEqual(150);

    assertTrue(venueSectionOptional.isPresent());
    assertEquals(200, venueSectionOptional.get().getAvailableSeats());
  }
}
