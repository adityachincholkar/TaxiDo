package com.uber.examples;

import com.uber.config.CustomUserDetails;
import com.uber.entities.Driver;
import com.uber.entities.Rider;
import com.uber.services.impl.CustomUserDetailsService;
import com.uber.services.impl.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Example showing how to authenticate users and generate JWT tokens with user ID
 */
@Component
public class AuthenticationExample {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtService jwtService;

    /**
     * Example authentication flow that generates JWT with user ID
     */
    public String authenticateAndGenerateToken(String email, String password) {
        // Step 1: Authenticate user credentials
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

        // Step 2: Load user details (this will be CustomUserDetails with ID)
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Step 3: Generate JWT token (ID will be automatically included)
        String token = jwtService.generateToken(userDetails);

        // Step 4: Extract and verify the user ID is in the token
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            Integer userId = jwtService.extractUserId(token);
            System.out.println("Generated token for user ID: " + userId);
            System.out.println("User ID matches: " + userId.equals(customUserDetails.getId()));
        }

        return token;
    }

    /**
     * Example of extracting user information from JWT token
     */
    public void extractUserInfoFromToken(String token) {
        // Extract user ID
        Integer userId = jwtService.extractUserId(token);
        
        // Extract username/email
        String username = jwtService.extractUsername(token);
        
        System.out.println("Token contains:");
        System.out.println("- User ID: " + userId);
        System.out.println("- Username: " + username);
        
        // You can now use the userId to fetch user details or verify permissions
        // For example: userService.findById(userId)
    }

    /**
     * Example of using user ID in authorization
     */
    public boolean canUserAccessResource(String token, Integer resourceOwnerId) {
        Integer tokenUserId = jwtService.extractUserId(token);
        
        // Check if the user from token matches the resource owner
        return tokenUserId != null && tokenUserId.equals(resourceOwnerId);
    }
}
