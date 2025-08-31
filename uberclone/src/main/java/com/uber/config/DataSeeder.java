package com.uber.config;

import com.uber.entities.Role;
import com.uber.repositories.RoleRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data seeding component to ensure required roles exist in the database
 * This runs automatically when the application starts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepo roleRepo;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
    }

    private void seedRoles() {
        // Create ROLE_RIDER if it doesn't exist
        if (roleRepo.findByName("ROLE_RIDER").isEmpty()) {
            Role riderRole = new Role();
            riderRole.setName("ROLE_RIDER");
            roleRepo.save(riderRole);
            log.info("Created ROLE_RIDER in database");
        } else {
            log.info("ROLE_RIDER already exists in database");
        }

        // Create ROLE_DRIVER if it doesn't exist
        if (roleRepo.findByName("ROLE_DRIVER").isEmpty()) {
            Role driverRole = new Role();
            driverRole.setName("ROLE_DRIVER");
            roleRepo.save(driverRole);
            log.info("Created ROLE_DRIVER in database");
        } else {
            log.info("ROLE_DRIVER already exists in database");
        }

        // Create ROLE_ADMIN if it doesn't exist (for future use)
        if (roleRepo.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepo.save(adminRole);
            log.info("Created ROLE_ADMIN in database");
        } else {
            log.info("ROLE_ADMIN already exists in database");
        }
    }
}
