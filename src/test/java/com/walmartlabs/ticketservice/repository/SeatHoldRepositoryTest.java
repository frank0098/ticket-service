package com.walmartlabs.ticketservice.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.walmartlabs.ticketservice.entity.SeatHold;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class SeatHoldRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private SeatHoldRepository seatHoldRepository;

  // Test findAllByCreatedAtBefore method.
  @Test
  public void testFindAllByCreatedAtBefore() throws InterruptedException {
    entityManager.persist(SeatHold.builder().build());
    entityManager.persist(SeatHold.builder().build());
    entityManager.flush();

    Date date = new Date();
    Thread.sleep(1000);

    entityManager.persist(SeatHold.builder().build());
    entityManager.persist(SeatHold.builder().build());
    entityManager.flush();

    assertTrue(seatHoldRepository.findAllByCreatedAtBefore(date).size() < 4);
    assertTrue(seatHoldRepository.findAllByCreatedAtBefore(new Date()).size() > 2);
  }
}
