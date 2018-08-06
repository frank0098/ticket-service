package com.walmartlabs.ticketservice.repository;

import com.walmartlabs.ticketservice.entity.Customer;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface CustomerRepository extends CrudRepository<Customer, Integer> {

  Optional<Customer> findByEmail(String email);
}
