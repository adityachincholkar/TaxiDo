package com.uber.controller;

import com.uber.entities.Ride;
import com.uber.exceptions.ResourceNotFoundException;
import com.uber.payloads.ApiResponse;
import com.uber.payloads.OrderResponseDto;
import com.uber.payloads.PaymentVerificationDto;
import com.uber.repositories.RideRepo;
import com.uber.services.impl.RazorpayService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/riders")
public class RazorPayController {
    private static final Logger logger = LoggerFactory.getLogger(RazorPayController.class);

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private RideRepo rideRepo;

    @PostMapping("/{rideId}/payment/initiate")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<?> createOrder(@PathVariable Long rideId) {
        try {
            logger.info("Initiating payment for ride: {}", rideId);
            Ride ride = rideRepo.findById(rideId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ride", "ride id", rideId));
            
            OrderResponseDto orderResponse = razorpayService.createOrder(ride.getFare(), rideId);
            if (orderResponse != null) {
                logger.info("Payment initiation successful for ride: {}", rideId);
                return ResponseEntity.ok(orderResponse);
            } else {
                logger.error("Failed to create Razorpay order for ride: {}", rideId);
                return ResponseEntity.badRequest().body("Failed to create Razorpay order.");
            }
        } catch (ResourceNotFoundException e) {
            logger.error("Ride not found: {}", rideId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error creating payment order for ride: {}", rideId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the order.");
        }
    }

    @PostMapping("/{rideId}/payment/verify")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<ApiResponse> verifyPayment(
            @Valid @RequestBody PaymentVerificationDto paymentVerificationDto,
            @PathVariable Long rideId) {
        try {
            logger.info("Verifying payment for ride: {}, orderId: {}", 
                    rideId, paymentVerificationDto.getRazorpayOrderId());

            boolean paymentVerified = razorpayService.verifyPayment(
                paymentVerificationDto.getRazorpayOrderId(),
                paymentVerificationDto.getRazorpayPaymentId(),
                paymentVerificationDto.getRazorpaySignature(),
                rideId
            );
            
            if (paymentVerified) {
                logger.info("Payment verification successful for ride: {}", rideId);
                return new ResponseEntity<>(new ApiResponse("Payment successful", true), HttpStatus.OK);
            } else {
                logger.warn("Payment verification failed for ride: {}", rideId);
                return new ResponseEntity<>(new ApiResponse("Payment verification failed", false), HttpStatus.BAD_REQUEST);
            }
        } catch (ResourceNotFoundException e) {
            logger.error("Ride not found during payment verification: {}", rideId, e);
            return new ResponseEntity<>(new ApiResponse("Ride not found", false), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error processing payment verification for ride: {}", rideId, e);
            return new ResponseEntity<>(new ApiResponse("Error processing payment verification", false), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
