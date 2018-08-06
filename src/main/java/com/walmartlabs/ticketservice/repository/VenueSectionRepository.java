package com.walmartlabs.ticketservice.repository;

import com.walmartlabs.ticketservice.entity.VenueSection;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface VenueSectionRepository extends CrudRepository<VenueSection, Integer> {

  List<VenueSection> findAll();

  /**
   * The field availableSeats can be read and written by multiple instances concurrently, so
   * pessimistic locking is required.
   *
   * Pessimistic locking is delegated to database so it also works for distributed systems.
   */

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<VenueSection> findAllByAvailableSeatsGreaterThan(int numSeats);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<VenueSection> findFirstByAvailableSeatsGreaterThanEqual(int numSeats);
}
