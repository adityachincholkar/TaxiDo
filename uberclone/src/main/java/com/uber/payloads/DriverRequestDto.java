package com.uber.payloads;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DriverRequestDto {

    @NotEmpty
    @Size(min = 2, message = "Username must of minimum 2 charectors")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email address is not valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{6,}$", message = "Requires Min length of 6 chars, a uppercase, a lowercase,a number, and a special charector")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^91[6-9]\\d{9}$", message = "Please, Enter a valid mobile number")
    @Size(min = 12, max = 12, message = "Phone number must be exactly 12 characters long")
    private String phoneNumber;

    @NotBlank(message = "License Plate number is required")
    private  String licensePlate;

    @NotBlank(message = "Car Model is required")
    private String carModel;

}
