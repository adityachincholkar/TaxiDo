package com.uber.payloads;

import jakarta.persistence.Column;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
public class DriverResponseDto {


    private int id;

    private String name;

    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String phoneNumber;

    private Double rating;

    private  String licensePlate;

    private String carModel;

    private Boolean isAvailable;
}
