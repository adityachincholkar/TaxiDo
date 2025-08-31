package com.uber.examples;

import com.uber.config.CustomUserDetails;
import com.uber.services.impl.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating how to use the JWT service with user ID functionality
 */
@Component
public class JwtUsageExample {

    @Autowired
    private JwtService jwtService;

    public void demonstrateJwtUsage() {
        // Example 1: Create a CustomUserDetails with user ID
        CustomUserDetails userDetails = new CustomUserDetails(
                123, // User ID
                "user@example.com", // Username/Email
                "encodedPassword", // Password
                Arrays.asList(new SimpleGrantedAuthority("ROLE_RIDER"))
        );

        // Example 2: Generate JWT token (ID will be automatically included)
        String token = jwtService.generateToken(userDetails);
        System.out.println("Generated JWT Token: " + token);

        // Example 3: Generate JWT token with additional claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "RIDER");
        extraClaims.put("verified", true);
        
        String tokenWithClaims = jwtService.generateToken(extraClaims, userDetails);
        System.out.println("JWT Token with extra claims: " + tokenWithClaims);

        // Example 4: Extract user ID from token
        Integer userId = jwtService.extractUserId(token);
        System.out.println("Extracted User ID: " + userId);

        // Example 5: Extract username from token
        String username = jwtService.extractUsername(token);
        System.out.println("Extracted Username: " + username);

        // Example 6: Validate token
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        System.out.println("Token is valid: " + isValid);

        // Example 7: Extract custom claims
        String role = jwtService.extractClaim(tokenWithClaims, claims -> claims.get("role", String.class));
        Boolean verified = jwtService.extractClaim(tokenWithClaims, claims -> claims.get("verified", Boolean.class));
        System.out.println("Extracted role: " + role);
        System.out.println("Extracted verified status: " + verified);
    }
}
