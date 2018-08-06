package com.walmartlabs.ticketservice.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.walmartlabs.ticketservice.entity.Customer;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class CustomerRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private CustomerRepository customerRepository;

  // Test findByEmail method.
  @Test
  public void testFindByEmail() {
    entityManager.persist(Customer.builder().email("test@test.com").build());
    entityManager.flush();
    Optional<Customer> customerOptional = customerRepository.findByEmail("test@test.com");

    assertTrue(customerOptional.isPresent());
    assertEquals("test@test.com", customerOptional.get().getEmail());
  }
}
