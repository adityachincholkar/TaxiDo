package com.uber.payloads;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyDriverResponseDto {

    private int driverId;
    private String name;
    private String email;
    private String carModel;
    private double distanceInKm;
    private LocationDto location;


}