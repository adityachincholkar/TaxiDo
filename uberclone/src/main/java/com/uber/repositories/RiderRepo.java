package com.uber.repositories;

import com.uber.entities.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface RiderRepo extends JpaRepository<Rider,Integer> {

    Optional<Rider> findByEmail(String email);

}
