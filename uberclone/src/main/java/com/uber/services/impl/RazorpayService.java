package com.uber.services.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.uber.entities.Ride;
import com.uber.entities.RideStatus;
import com.uber.exceptions.ResourceNotFoundException;
import com.uber.payloads.OrderResponseDto;
import com.uber.repositories.RideRepo;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RazorpayService {
    private static final Logger logger = LoggerFactory.getLogger(RazorpayService.class);
    
    private RazorpayClient razorpayClient;

    @Autowired
    private RideRepo rideRepo;

    @Value("${razorpay.api.secret}")
    private String secretKey;

    @Value("${razorpay.api.key}")
    private String publishableKey;

    @Value("${razorpay.currency}")
    private String currency;


    @PostConstruct
    private void initializeRazorpayClient() {
        try {
            razorpayClient = new RazorpayClient(publishableKey, secretKey);
            logger.info("Razorpay client initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Razorpay client", e);
            throw new RuntimeException("Razorpay initialization failed", e);
        }
    }


    public OrderResponseDto createOrder(double amount, Long rideId) {
        try {
            // Safe currency conversion using BigDecimal
            long amountInPaise = BigDecimal.valueOf(amount)
                    .movePointRight(2)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();

            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", currency);
            options.put("receipt", "ride-" + rideId); // Unique receipt per ride
            options.put("payment_capture", 1);

            // Call Razorpay's Orders API to create an order
            Order razorpayOrder = razorpayClient.orders.create(options);
            String orderId = razorpayOrder.get("id");

            // Save order ID to ride
            Ride ride = rideRepo.findById(rideId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ride", "rideId", rideId));
            ride.setRazorpayOrderId(orderId);
            rideRepo.save(ride);

            logger.info("Razorpay order created successfully for ride: {}, orderId: {}", rideId, orderId);

            return OrderResponseDto.builder()
                    .orderId(orderId)
                    .amount(amountInPaise)
                    .currency(razorpayOrder.get("currency"))
                    .apiKey(publishableKey)
                    .build();
        } catch (Exception e) {
            logger.error("Failed to create Razorpay order for ride: {}", rideId, e);
            return null;
        }
    }


    public boolean verifyPayment(String orderId, String paymentId, String razorpaySignature, Long rideId) {
        try {
            // Load ride and validate
            Ride ride = rideRepo.findById(rideId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ride", "rideId", rideId));

            // Critical security check: ensure orderId matches the ride's saved orderId
            if (!orderId.equals(ride.getRazorpayOrderId())) {
                logger.warn("Order ID mismatch for ride {}: expected {}, got {}", 
                        rideId, ride.getRazorpayOrderId(), orderId);
                return false;
            }

            // Idempotency check: if already paid, return true
            if (RideStatus.PAID.equals(ride.getStatus())) {
                logger.info("Ride {} is already marked as PAID", rideId);
                return true;
            }

            // Verify signature with Razorpay
            String payload = orderId + '|' + paymentId;
            boolean isSignatureValid = Utils.verifySignature(payload, razorpaySignature, secretKey);

            if (isSignatureValid) {
                // Update ride status to PAID
                ride.setStatus(RideStatus.PAID);
                rideRepo.save(ride);
                logger.info("Payment verified successfully for ride: {}, orderId: {}", rideId, orderId);
                return true;
            } else {
                logger.warn("Payment signature verification failed for ride: {}, orderId: {}", rideId, orderId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error verifying payment for ride: {}, orderId: {}", rideId, orderId, e);
            return false;
        }
    }
}
