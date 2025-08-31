package com.uber.services.impl;

import com.uber.config.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Secret key for signing JWT tokens - should be stored in application.properties
    @Value("${jwt.secret:mySecretKey}")
    private String secretKey;

    // Token expiration time in milliseconds (24 hours)
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    // Generate secret key for JWT signing
    private SecretKey getSigningKey() {
        // Create a secure key from the secret string
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Extract username from JWT token
    public String extractUsername(String token) {
        // Get the subject claim from token (contains username)
        return extractClaim(token, Claims::getSubject);
    }

    // Extract user ID from JWT token
    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("id", Integer.class));
    }

    // Generic method to extract any claim from JWT token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims); // Apply the function to get specific claim
    }

    // Generate JWT token for user
    public String generateToken(UserDetails userDetails) {
        // Create token with empty extra claims
        return generateToken(new HashMap<>(), userDetails);
    }

    // Generate JWT token with extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    // Build the actual JWT token
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        // Extract user ID if available
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            extraClaims.put("id", customUserDetails.getId());
        }
        
        return Jwts
                .builder()
                .setClaims(extraClaims)              // Set custom claims
                .setSubject(userDetails.getUsername()) // Set username as subject
                .setIssuedAt(new Date(System.currentTimeMillis())) // Set creation time
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Set expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Sign with secret key
                .compact(); // Convert to string
    }

    // Validate JWT token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Check if username matches and token is not expired
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        // Compare expiration date with current time
        return extractExpiration(token).before(new Date());
    }

    // Extract expiration date from token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract all claims from JWT token
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey()) // Set the signing key for verification
                .build()
                .parseClaimsJws(token) // Parse and verify the token
                .getBody(); // Get the claims
    }
}
