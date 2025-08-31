package com.uber.services;


import com.uber.entities.RideStatus;
import com.uber.payloads.*;
import java.util.List;

public interface RideService {

    RideResponseDto requestRide(String email,RideRequestDto rideRequestDto);

    PendingRideResponseDto checkRideStatus(String email, RideStatus status);

    PendingRideResponseDto acceptRide(Long rideId);

    PendingRideResponseDto startRide(Long rideId);

    PendingRideResponseDto completeRide(Long rideId);

    PendingRideResponseDto cancelRide(Long rideId);

    void RateRide(Long rideId, RatingDto ratingDto);

    EstimatedFairDto calculateEstimatedFair(RideRequestDto rideRequestDto);

    List<RideResponseDto> getRideHistory(String userEmail, Integer pageNumber, Integer pageSize);

    List<RideResponseDto> getDriverRideHistory(String userEmail, Integer pageNumber, Integer pageSize);
}
