package com.uber.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private String orderId;    // The unique ID from Razorpay
    private Long amount;       // The amount in the smallest currency unit (e.g., paise)
    private String currency;   // e.g., "INR"
    private String apiKey;     // Your public Razorpay Key ID
}
