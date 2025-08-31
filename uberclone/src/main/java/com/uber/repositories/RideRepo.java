package com.uber.repositories;

import com.uber.entities.Ride;
import com.uber.entities.RideStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RideRepo extends JpaRepository<Ride,Long> {

    Optional<Ride> findByDriver_IdAndStatus(Integer driverId, RideStatus status);
    List<Ride> findByRider_IdAndStatusIn(Integer riderId, Collection<RideStatus> statuses);
    Page<Ride> findByRider_IdOrderByCreatedAtAsc(Integer rideId, Pageable pageable);
    Page<Ride> findByDriver_IdOrderByCreatedAtAsc(Integer rideId, Pageable pageable);

}
