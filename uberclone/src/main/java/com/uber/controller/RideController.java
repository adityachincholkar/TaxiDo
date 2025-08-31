package com.uber.controller;

import com.uber.payloads.*;
import com.uber.repositories.RideRepo;
import com.uber.services.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RideService rideService;

    @PostMapping("/me/requestride")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<RideResponseDto> requestRide(@RequestBody RideRequestDto rideRequestDto){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        RideResponseDto rideDetails = rideService.requestRide(userEmail,rideRequestDto);
        return  new ResponseEntity<>(rideDetails, HttpStatus.ACCEPTED);
    }

    @PostMapping("/me/{rideId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public  ResponseEntity<PendingRideResponseDto> updateRideStatus(@PathVariable("rideId") Long rideId){
        PendingRideResponseDto rideResponse = rideService.acceptRide(rideId);
        return new ResponseEntity<PendingRideResponseDto>(rideResponse,HttpStatus.OK);
    }

    @PostMapping("/me/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public  ResponseEntity<PendingRideResponseDto> startRide(@PathVariable("rideId") Long rideId){
        PendingRideResponseDto rideResponse = rideService.startRide(rideId);
        return new ResponseEntity<PendingRideResponseDto>(rideResponse,HttpStatus.OK);
    }


    @PostMapping("/me/{rideId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public  ResponseEntity<PendingRideResponseDto> completeRide(@PathVariable("rideId") Long rideId){
        PendingRideResponseDto rideResponse = rideService.completeRide(rideId);
        return new ResponseEntity<PendingRideResponseDto>(rideResponse,HttpStatus.OK);
    }

    @PostMapping("/me/{rideId}/cancel")
    @PreAuthorize("hasRole('DRIVER') or hasRole('RIDER')")
    public  ResponseEntity<PendingRideResponseDto> cancelRide(@PathVariable("rideId") Long rideId){
        PendingRideResponseDto rideResponse = rideService.cancelRide(rideId);
        return new ResponseEntity<PendingRideResponseDto>(rideResponse,HttpStatus.OK);
    }

    @PostMapping("/me/{rideId}/rate")
    @PreAuthorize("hasRole('DRIVER') or hasRole('RIDER')")
    public ResponseEntity<ApiResponse> rateRide(@PathVariable("rideId") Long rideId,@RequestBody RatingDto ratingDto){
        rideService.RateRide(rideId, ratingDto);
        return new  ResponseEntity<ApiResponse>(new ApiResponse("The ride has been rated", true ), HttpStatus.OK);
    }

    @PostMapping("/me/estimateFair")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<EstimatedFairDto> estimateFair(@RequestBody RideRequestDto rideRequestDto){
        EstimatedFairDto estimatedFairDto =  rideService.calculateEstimatedFair(rideRequestDto);
        return new  ResponseEntity<>(estimatedFairDto,HttpStatus.OK);
    }




}
