package com.uber.controller;

import com.uber.payloads.*;
import com.uber.services.RideService;
import com.uber.services.RiderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.swing.plaf.PanelUI;
import java.util.List;

@RestController
@RequestMapping("/api/riders")
public class RiderController {

    @Autowired
    private RiderService riderService;

    @Autowired
    private RideService rideService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<RiderResponseDto>> getAllRiders(){
        List<RiderResponseDto> allRiders = riderService.getAllRiders();
        return new ResponseEntity<List<RiderResponseDto>>(allRiders,HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<RiderResponseDto> getRiderInfo(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        RiderResponseDto user = riderService.findRider(userEmail);
        return new  ResponseEntity<RiderResponseDto>(user,HttpStatus.OK);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<RiderResponseDto> updateRiderById(@Valid @RequestBody RiderRequestDto riderRequestDto){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        RiderResponseDto User = riderService.updateRider(riderRequestDto,userEmail);
        return new ResponseEntity<>(User,HttpStatus.OK);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<?> deleteRider(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        riderService.deleteRider(userEmail);
        return new ResponseEntity<ApiResponse>(new ApiResponse("Rider deleted Successfully",true),HttpStatus.OK);
    }


    @PostMapping("/me/nearby-drivers")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<List<NearbyDriverResponseDto>> getNearByDrivers(@RequestBody LocationDto dto){
        List<NearbyDriverResponseDto> nearByDrivers = riderService.getNearByDrivers(dto.getLongitude(),dto.getLatitude(),5);
        return new ResponseEntity<List<NearbyDriverResponseDto>>(nearByDrivers,HttpStatus.OK);
    }

    @GetMapping("/me/rides")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<List<RideResponseDto>> getRideHistory(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        String riderEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<RideResponseDto> history = rideService.getRideHistory(riderEmail, pageNumber, pageSize);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }


}
