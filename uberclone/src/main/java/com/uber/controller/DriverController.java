package com.uber.controller;

import com.uber.entities.RideStatus;
import com.uber.payloads.*;
import com.uber.services.DriverService;
import com.uber.services.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/drivers")
public class DriverController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RideService rideService;

    private static final String GEO_KEY = "places";

    private final DriverService driverService;



    public DriverController(DriverService driverService, RideService rideService) {
        this.driverService = driverService;
        this.rideService = rideService;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<DriverResponseDto>> getAllDrivers(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<DriverResponseDto> drivers = driverService.getAllDrivers();
        return new ResponseEntity<List<DriverResponseDto>>(drivers,HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<DriverResponseDto> getDriverById(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        DriverResponseDto driver = driverService.getDriver(userEmail);
        return new  ResponseEntity<DriverResponseDto>(driver,HttpStatus.OK);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> deleteDriverById(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        driverService.deleteDriver(userEmail);
        return new ResponseEntity<ApiResponse>(new ApiResponse("Driver deleted successfully",true),HttpStatus.OK);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponseDto> updateDriver( @RequestBody DriverRequestDto driverRequestDto ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        DriverResponseDto updatedDriver = driverService.updateDriverById(userEmail, driverRequestDto);
        return new ResponseEntity<DriverResponseDto>(updatedDriver, HttpStatus.OK);
    }

    @PutMapping("/me/availability")
    public  ResponseEntity<DriverResponseDto> isDriverAvailable(@RequestBody AvailableDto availableDto){
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = auth.getName();
    DriverResponseDto isDriverAvailable = driverService.isDriverAvaible(availableDto,userEmail);
    return new ResponseEntity<DriverResponseDto>(isDriverAvailable, HttpStatus.OK);

    }

    @GetMapping("/me/rides/pending")
    public ResponseEntity<?> getMyRides(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        PendingRideResponseDto pendingRideResponseDto = rideService.checkRideStatus(userEmail, RideStatus.REQUESTED);
        
        if (pendingRideResponseDto == null) {
            return new ResponseEntity<>(new ApiResponse("No pending rides found", false), HttpStatus.OK);
        }
        
        return new ResponseEntity<>(pendingRideResponseDto, HttpStatus.OK);
    }

    @GetMapping("/me/rides")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<RideResponseDto>> getRideHistory(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        String DriverEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<RideResponseDto> history = rideService.getDriverRideHistory(DriverEmail, pageNumber, pageSize);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }



}
