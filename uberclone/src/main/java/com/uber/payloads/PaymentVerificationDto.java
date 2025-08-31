package com.uber.payloads;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentVerificationDto {
    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;
    
    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;
    
    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}
