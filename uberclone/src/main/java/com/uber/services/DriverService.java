package com.uber.services;

import com.uber.payloads.AvailableDto;
import com.uber.payloads.DriverRequestDto;
import com.uber.payloads.DriverResponseDto;

import java.util.List;

public interface DriverService {
    DriverResponseDto createDriver(DriverRequestDto driverRequestDto);

    List<DriverResponseDto> getAllDrivers();

    DriverResponseDto getDriver(String email);

    DriverResponseDto updateDriverById(String email,DriverRequestDto driverRequestDto);

    void deleteDriver(String email);

    DriverResponseDto isDriverAvaible(AvailableDto availableDto,String email);
}
