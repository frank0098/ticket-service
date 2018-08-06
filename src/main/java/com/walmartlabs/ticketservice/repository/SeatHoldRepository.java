package com.walmartlabs.ticketservice.repository;

import com.walmartlabs.ticketservice.entity.SeatHold;
import java.util.Date;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface SeatHoldRepository extends CrudRepository<SeatHold, Integer> {

  List<SeatHold> findAllByCreatedAtBefore(Date date);
}
