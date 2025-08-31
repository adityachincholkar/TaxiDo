package com.uber.payloads;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderResponseDto {
    private int id;

    private String name;

    private String email;

    private String phoneNumber;

    private Double rating;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
