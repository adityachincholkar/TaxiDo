package com.uber.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RideRequestDto {
    // You can reuse the LocationDto from the nearby-drivers feature
    private LocationDto pickupLocation;
    private LocationDto dropoffLocation;
}