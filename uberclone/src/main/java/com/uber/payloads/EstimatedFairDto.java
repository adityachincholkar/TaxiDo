package com.uber.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstimatedFairDto {
    private Double estimatedFare;
    private String currency;
    private Double distanceInKm;
}
