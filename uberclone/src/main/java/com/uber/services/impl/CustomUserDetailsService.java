package com.uber.services.impl;

import com.uber.config.CustomUserDetails;
import com.uber.entities.Driver;
import com.uber.entities.Rider;
import com.uber.repositories.DriverRepo;
import com.uber.repositories.RiderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final RiderRepo riderRepo;
    private final DriverRepo driverRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Check Rider repository
        Optional<Rider> rider = riderRepo.findByEmail(email);
        if (rider.isPresent()) {
            Rider r = rider.get();
            return new CustomUserDetails(r.getId(), r.getEmail(), r.getPassword(), r.getAuthorities());
        }

        // Check Driver repository
        Optional<Driver> driver = driverRepo.findByEmail(email);
        if (driver.isPresent()) {
            Driver d = driver.get();
            return new CustomUserDetails(d.getId(), d.getEmail(), d.getPassword(), d.getAuthorities());
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
