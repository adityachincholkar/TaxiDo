package com.uber.services.impl;

import com.uber.entities.Driver;
import com.uber.entities.Role;
import com.uber.exceptions.ResourceNotFoundException;
import com.uber.mapper.DriverMapper;
import com.uber.payloads.AvailableDto;
import com.uber.payloads.DriverRequestDto;
import com.uber.payloads.DriverResponseDto;
import com.uber.repositories.DriverRepo;
import com.uber.repositories.RoleRepo;
import com.uber.services.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepo driverRepo;
    
    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private DriverMapper driverMapper;

    @Override
    public DriverResponseDto createDriver(DriverRequestDto driverRequestDto) {
        Driver driver = driverMapper.toDriver(driverRequestDto);
        
        // Assign ROLE_DRIVER to the new driver
        Role driverRole = roleRepo.findByName("ROLE_DRIVER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("ROLE_DRIVER");
            return roleRepo.save(newRole);
        });
        driver.setRoles(Collections.singleton(driverRole));
        
        Driver created = driverRepo.save(driver);
        return driverMapper.toDriverResponseDto(created);
    }

    @Override
    public List<DriverResponseDto> getAllDrivers() {
        List<Driver> allDrivers = driverRepo.findAll();
        return allDrivers.stream().map(driverMapper::toDriverResponseDto).toList();
    }

    @Override
    public DriverResponseDto getDriver(String email) {
        Driver driver = driverRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("Driver", "driverId" +email,0));
        return driverMapper.toDriverResponseDto(driver);
    }

    @Override
    public DriverResponseDto updateDriverById(String email, DriverRequestDto driverRequestDto) {
        Driver currentDriver = driverRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("Driver", "driverId" + email,0));
        driverMapper.updateDriverFromDto(driverRequestDto,currentDriver);
        Driver updatedDriver = driverRepo.save(currentDriver);
        return driverMapper.toDriverResponseDto(updatedDriver);
    }

    @Override
    public void deleteDriver(String email) {
        Driver existingUser = driverRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("Driver", "driverId" + email,0));
        driverRepo.delete(existingUser);
    }

    @Override
    public DriverResponseDto isDriverAvaible(AvailableDto availableDto, String email) {
        Driver driver = driverRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("Driver","driverId" + email,0));
        driver.setIsAvailable(availableDto.getIsAvailable());
        driverRepo.save(driver);
        return driverMapper.toDriverResponseDto(driver);
    }


}
