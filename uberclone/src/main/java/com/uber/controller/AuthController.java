package com.uber.controller;

import com.uber.payloads.*;
import com.uber.services.DriverService;
import com.uber.services.RiderService;
import com.uber.services.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uber.payloads.AuthRequestDto;
import com.uber.payloads.AuthResponseDto;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RiderService riderService;
    private final DriverService driverService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto authRequestDto) {
        AuthResponseDto response = authService.authenticate(authRequestDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    /**
     * Public endpoint for rider registration
     */
    @PostMapping("/register/rider")
    public ResponseEntity<RiderResponseDto> registerRider(@Valid @RequestBody RiderRequestDto riderRequestDto){
        RiderResponseDto createdRider = riderService.createRider(riderRequestDto);
        return new ResponseEntity<>(createdRider, HttpStatus.CREATED);
    }

    /**
     * Public endpoint for driver registration
     */
    @PostMapping("/register/driver")
    public ResponseEntity<DriverResponseDto> registerDriver(@Valid @RequestBody DriverRequestDto driverRequestDto){
        DriverResponseDto createdDriver = driverService.createDriver(driverRequestDto);
        return new ResponseEntity<>(createdDriver, HttpStatus.CREATED);
    }
}






