package com.uber.payloads;

import com.uber.entities.RideStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideResponseDto {

    private Long rideId;
    private Integer riderId;
    private Integer driverId; // Can be null
    private RideStatus status;
    private LocationDto pickupLocation;
    private LocationDto dropoffLocation;
    private LocalDateTime createdAt;

}
