package com.uber.config;

import com.uber.services.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Add JWT filter

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless sessions for JWT
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/ws/**").permitAll()
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**").permitAll()

                        // Allow public access to health check or documentation endpoints
                        .requestMatchers("/api/public/**", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()


                        // Only drivers can access driver endpoints (all HTTP methods)
                        .requestMatchers("/api/drivers/**").hasAnyRole("DRIVER","ADMIN")

                        // Only riders can access rider endpoints (all HTTP methods)
                        .requestMatchers("/api/riders/**").hasAnyRole("RIDER","ADMIN")

                        // Ride endpoints are authenticated; method-level @PreAuthorize limits roles per action
                        .requestMatchers("/api/rides/**").authenticated()

                        .anyRequest().authenticated()
                )
                // Set the authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before the standard authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Remove HTTP Basic authentication as we're now using JWT
        // .httpBasic(Customizer.withDefaults()); // Comment this out

        return http.build();
    }
}

