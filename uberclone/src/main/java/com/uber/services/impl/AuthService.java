package com.uber.services.impl;


import com.uber.payloads.AuthRequestDto;
import com.uber.payloads.AuthResponseDto;
import com.uber.repositories.DriverRepo;
import com.uber.repositories.RiderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RiderRepo riderRepo;
    @Autowired
    private DriverRepo driverRepo;

    public AuthResponseDto authenticate(AuthRequestDto authRequestDto) {
        // 1. User authentication via AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDto.getEmail(),
                        authRequestDto.getPassword()
                )
        );
        UserDetails user = (UserDetails) authentication.getPrincipal();

        // 2. Create JWT
        String jwt = jwtService.generateToken(user);

        // 3. Return response DTO
        AuthResponseDto response = new AuthResponseDto();
        response.setToken(jwt);
        response.setTokenType("Bearer");
        return response;
    }
}
