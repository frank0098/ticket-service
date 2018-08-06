package com.walmartlabs.ticketservice.repository;

import com.walmartlabs.ticketservice.entity.Reservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ReservationRepository extends CrudRepository<Reservation, String> {

}
