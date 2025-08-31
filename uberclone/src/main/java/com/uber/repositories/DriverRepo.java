package com.uber.repositories;

import com.uber.entities.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepo extends JpaRepository<Driver,Integer> {
    Optional<Driver> findByEmail(String email);

}
