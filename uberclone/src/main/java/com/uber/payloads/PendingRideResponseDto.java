package com.uber.payloads;

import com.uber.entities.RideStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PendingRideResponseDto {

    private Long rideId;
    private Integer riderId;
    private String riderName;
    private RideStatus status;
    private LocationDto pickupLocation;
    private LocationDto dropoffLocation;
    private LocalDateTime createdAt;
}
