package com.uber.services.impl;

import com.uber.entities.Driver;
import com.uber.entities.Ride;
import com.uber.entities.RideStatus;
import com.uber.entities.Rider;
import com.uber.exceptions.ResourceNotFoundException;
import com.uber.exceptions.RideStateException;
import com.uber.payloads.*;
import com.uber.repositories.DriverRepo;
import com.uber.repositories.RideRepo;
import com.uber.repositories.RiderRepo;
import com.uber.services.RideService;
import com.uber.services.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class RideServiceImpl implements RideService {

    @Autowired
    private RideRepo rideRepo;

    @Autowired
    private RiderRepo riderRepo;

    @Autowired
    private DriverRepo driverRepo;

    @Autowired
    private RiderService riderService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final double EARTH_RADIUS = 6371.0; // in kilometers

    public static final double BASE_FARE = 19.0;

    public static final double PER_KILOMETER_RATE = 10.0;

    @Override
    public RideResponseDto requestRide(String email, RideRequestDto rideRequestDto) {
        Rider rider = riderRepo.findByEmail(email).
                orElseThrow(()-> new ResourceNotFoundException("User", "with emailid" + email , 0));
        List<Ride> activeRides = rideRepo.findByRider_IdAndStatusIn(
                rider.getId(),
                List.of(RideStatus.REQUESTED, RideStatus.ACCEPTED, RideStatus.IN_PROGRESS,RideStatus.COMPLETED)
        );

        if (activeRides.size() >= 1){
            throw new RideStateException("You already have an active ride. Please complete or cancel your current ride before requesting a new one");
        }


        List<NearbyDriverResponseDto> nearByDrivers = riderService.getNearByDrivers(
                rideRequestDto.getPickupLocation().getLongitude(),
                rideRequestDto.getPickupLocation().getLatitude(),
                10);

        if (nearByDrivers.isEmpty()){
            throw new ResourceNotFoundException("No available drivers found near you. Please try again later" , "" , 0 );
        }

        NearbyDriverResponseDto closestDriverDto = nearByDrivers.get(0);

        Driver driver = driverRepo.findByEmail(closestDriverDto.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("Driver", "email" +  closestDriverDto.getEmail() , 0 ));

        Ride ride =  Ride.builder()
                .rider(rider)
                .driver(driver)
                .pickupLongitude(rideRequestDto.getPickupLocation().getLongitude())
                .pickupLatitude(rideRequestDto.getPickupLocation().getLatitude())
                .dropoffLongitude(rideRequestDto.getDropoffLocation().getLongitude())
                .dropoffLatitude(rideRequestDto.getDropoffLocation().getLatitude())
                .status(RideStatus.REQUESTED)
                .build();

        Ride savedRide = rideRepo.save(ride);

        String destination = "/user/" + savedRide.getDriver().getEmail() + "/ride-request";
        simpMessagingTemplate.convertAndSend(destination, savedRide.getId());

        RideResponseDto rideResponseDto = RideResponseDto.builder()
                .rideId(savedRide.getId())
                .riderId(savedRide.getRider().getId())
                .driverId(savedRide.getDriver() != null ? savedRide.getDriver().getId() : null)
                .pickupLocation(new LocationDto(savedRide.getPickupLongitude(), savedRide.getPickupLatitude()))
                .dropoffLocation(new LocationDto(savedRide.getDropoffLongitude(), savedRide.getDropoffLatitude()))
                .status(savedRide.getStatus())
                .createdAt(savedRide.getCreatedAt())
                .build();

        return rideResponseDto;
    }

    @Override
    public PendingRideResponseDto checkRideStatus(String email, RideStatus rideStatus) {
        // Find driver by email first, then get rides by driver ID
        Driver driver = driverRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "email", 0));
        
        Optional<Ride> rideOptional = rideRepo.findByDriver_IdAndStatus(driver.getId(), rideStatus);
        if (rideOptional.isEmpty()) {
            return null; // No ride found with the given status
        }
        
        Ride ride = rideOptional.get();

        PendingRideResponseDto pendingRideResponseDto = PendingRideResponseDto.builder()
                .rideId(ride.getId())
                .riderId(ride.getRider().getId())
                .riderName(ride.getRider().getName())
                .pickupLocation(new LocationDto(ride.getPickupLongitude(), ride.getPickupLatitude()))
                .dropoffLocation(new LocationDto(ride.getDropoffLongitude(), ride.getDropoffLatitude()))
                .status(ride.getStatus())
                .createdAt(ride.getCreatedAt())
                .build();

        return pendingRideResponseDto;
    }

    @Override
    public PendingRideResponseDto acceptRide(Long rideId) {
        Ride ride = rideRepo.findById(rideId).orElseThrow(()-> new ResourceNotFoundException("Ride", "with rideId", rideId));

        if (ride.getStatus() != RideStatus.REQUESTED){
            throw new RideStateException("Ride cannot be accepted if its not in pending state");
        }
        ride.setStatus(RideStatus.ACCEPTED);
        
        // Save the updated ride status to database
        Ride savedRide = rideRepo.save(ride);
        
        PendingRideResponseDto UpdatedStatus = PendingRideResponseDto.builder()
                .rideId(savedRide.getId())
                .riderId(savedRide.getRider().getId())
                .riderName(savedRide.getRider().getName())
                .pickupLocation(new LocationDto(savedRide.getPickupLongitude(), savedRide.getPickupLatitude()))
                .dropoffLocation(new LocationDto(savedRide.getDropoffLongitude(), savedRide.getDropoffLatitude()))
                .status(savedRide.getStatus())
                .createdAt(savedRide.getCreatedAt())
                .build();
                
        return UpdatedStatus;
    }

    @Override
    public PendingRideResponseDto startRide(Long rideId) {
        Ride ride = rideRepo.findById(rideId).orElseThrow(()-> new ResourceNotFoundException("Ride", "with rideId", rideId));


        if (ride.getStatus() != RideStatus.ACCEPTED){
            throw new RideStateException("Ride cannot be in progress if its not in accepted state");
        }

        ride.setStatus(RideStatus.IN_PROGRESS);

        // Save the updated ride status to database
        Ride savedRide = rideRepo.save(ride);

        PendingRideResponseDto UpdatedStatus = PendingRideResponseDto.builder()
                .rideId(savedRide.getId())
                .riderId(savedRide.getRider().getId())
                .riderName(savedRide.getRider().getName())
                .pickupLocation(new LocationDto(savedRide.getPickupLongitude(), savedRide.getPickupLatitude()))
                .dropoffLocation(new LocationDto(savedRide.getDropoffLongitude(), savedRide.getDropoffLatitude()))
                .status(savedRide.getStatus())
                .createdAt(savedRide.getCreatedAt())
                .build();

        return UpdatedStatus;
    }

    @Override
    public PendingRideResponseDto completeRide(Long rideId) {
        Ride ride = rideRepo.findById(rideId).orElseThrow(()-> new ResourceNotFoundException("Ride", "with rideId", rideId));

        if (ride.getStatus() != RideStatus.IN_PROGRESS){
            throw new RideStateException("Ride cannot be completed if its not in progress state");
        }
        ride.setStatus(RideStatus.COMPLETED);

        EstimatedFairDto estimatedFairDto = calculateEstimatedFair(new RideRequestDto(new LocationDto(ride.getPickupLongitude(),ride.getPickupLatitude()),new LocationDto(ride.getDropoffLongitude(),ride.getDropoffLatitude())));

        ride.setFare(estimatedFairDto.getEstimatedFare());
        // Save the updated ride status to database

        Ride savedRide = rideRepo.save(ride);

        PendingRideResponseDto UpdatedStatus = PendingRideResponseDto.builder()
                .rideId(savedRide.getId())
                .riderId(savedRide.getRider().getId())
                .riderName(savedRide.getRider().getName())
                .pickupLocation(new LocationDto(savedRide.getPickupLongitude(), savedRide.getPickupLatitude()))
                .dropoffLocation(new LocationDto(savedRide.getDropoffLongitude(), savedRide.getDropoffLatitude()))
                .status(savedRide.getStatus())
                .createdAt(savedRide.getCreatedAt())
                .build();

        return UpdatedStatus;
    }

    @Override
    public PendingRideResponseDto cancelRide(Long rideId) {
        Ride ride = rideRepo.findById(rideId).orElseThrow(()-> new ResourceNotFoundException("Ride", "with rideId", rideId));

        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED){
            throw new RideStateException("Ride cannot be cancelled once it is COMPLETED or already CANCELLED");
        }

        ride.setStatus(RideStatus.CANCELLED);

        // Save the updated ride status to database
        Ride savedRide = rideRepo.save(ride);

        PendingRideResponseDto UpdatedStatus = PendingRideResponseDto.builder()
                .rideId(savedRide.getId())
                .riderId(savedRide.getRider().getId())
                .riderName(savedRide.getRider().getName())
                .pickupLocation(new LocationDto(savedRide.getPickupLongitude(), savedRide.getPickupLatitude()))
                .dropoffLocation(new LocationDto(savedRide.getDropoffLongitude(), savedRide.getDropoffLatitude()))
                .status(savedRide.getStatus())
                .createdAt(savedRide.getCreatedAt())
                .build();

        return UpdatedStatus;
    }

    @Override
    public void RateRide(Long rideId, RatingDto ratingDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", "with rideId", rideId));

        if (!ride.getStatus().equals(RideStatus.COMPLETED)) {
            throw new RideStateException("You cannot rate a ride that is not completed");
        }

        // Check if user is Rider
        riderRepo.findByEmail(userEmail).ifPresent(rider -> {
            if (!ride.getRider().equals(rider)) {
                throw new SecurityException("You are not authorised to rate this ride");
            }
            updateDriverRating(ride.getDriver(), ratingDto.getRating());
        });

        // Check if user is Driver
        driverRepo.findByEmail(userEmail).ifPresent(driver -> {
            if (!ride.getDriver().equals(driver)) {
                throw new SecurityException("You are not authorised to rate this ride");
            }
            updateRiderRating(ride.getRider(), ratingDto.getRating());
        });
    }

    @Override
    public EstimatedFairDto calculateEstimatedFair(RideRequestDto rideRequestDto) {
        double distance = calculateDistance(rideRequestDto.getPickupLocation().getLatitude(),
                rideRequestDto.getPickupLocation().getLongitude(),
                rideRequestDto.getDropoffLocation().getLatitude(),
                rideRequestDto.getDropoffLocation().getLongitude()
        );
        double eastimatedFair = BASE_FARE + (distance * PER_KILOMETER_RATE);

        double finalFare = Math.round(eastimatedFair * 100.0) / 100.0;
        double finalDistance = Math.round(distance * 100.0) / 100.0;

        return new EstimatedFairDto(finalFare, "INR", finalDistance);
    }

    double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    private double calculateDistance(double startLat, double startLong, double endLat, double endLong) {

        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private void updateDriverRating(Driver driver, double newRating) {
        double currentRating = driver.getRating();
        int ratingCount =  driver.getRatingCount();
        double newAverage = ((currentRating * (double)ratingCount) + newRating) / ((double)(ratingCount + 1));
        driver.setRating(newAverage);
        driver.setRatingCount(ratingCount + 1);
        driverRepo.save(driver);
    }

    private void updateRiderRating(Rider rider, double newRating) {
        double currentRating = rider.getRating();
        int ratingCount = rider.getRatingCount();
        double newAverage = ((currentRating * (double) ratingCount) + newRating) / ((double) ratingCount + 1);
        rider.setRating(newAverage);
        rider.setRatingCount(ratingCount + 1);
        riderRepo.save(rider);
    }

    @Override
    public List<RideResponseDto> getRideHistory(String userEmail, Integer pageNumber, Integer pageSize) {
        Rider rider = riderRepo.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "with username " + userEmail, 0));
        Pageable page = PageRequest.of(pageNumber, pageSize);
        Page<Ride> rideHistory = rideRepo.findByRider_IdOrderByCreatedAtAsc(rider.getId(), page);
        List<Ride> rideList = rideHistory.getContent();
        
        List<RideResponseDto> responseDtoList = rideList.stream().map((ride) -> {
            return RideResponseDto.builder()
                    .rideId(ride.getId())
                    .riderId(ride.getRider().getId())
                    .driverId(ride.getDriver() != null ? ride.getDriver().getId() : null)
                    .status(ride.getStatus())
                    .pickupLocation(new LocationDto(ride.getPickupLongitude(), ride.getPickupLatitude()))
                    .dropoffLocation(new LocationDto(ride.getDropoffLongitude(), ride.getDropoffLatitude()))
                    .createdAt(ride.getCreatedAt())
                    .build();
        }).toList();
        
        return responseDtoList;
    }

    @Override
    public List<RideResponseDto> getDriverRideHistory(String userEmail, Integer pageNumber, Integer pageSize) {
        Driver driver = driverRepo.findByEmail(userEmail)
                .orElseThrow(()-> new ResourceNotFoundException("Driver","email" + userEmail, 0));
        Pageable page = PageRequest.of(pageNumber,pageSize);
        Page<Ride> rideHistory = rideRepo.findByDriver_IdOrderByCreatedAtAsc(driver.getId(),page);
        List<Ride> rideList = rideHistory.getContent();
        List<RideResponseDto> responseDtoList = rideList.stream().map((ride) -> {
            return RideResponseDto.builder()
                    .rideId(ride.getId())
                    .riderId(ride.getRider().getId())
                    .driverId(ride.getDriver() != null ? ride.getDriver().getId() : null)
                    .status(ride.getStatus())
                    .pickupLocation(new LocationDto(ride.getPickupLongitude(), ride.getPickupLatitude()))
                    .dropoffLocation(new LocationDto(ride.getDropoffLongitude(), ride.getDropoffLatitude()))
                    .createdAt(ride.getCreatedAt())
                    .build();
        }).toList();

        return responseDtoList;
    }

}
