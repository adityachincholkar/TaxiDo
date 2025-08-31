package com.uber.controller;

import com.uber.payloads.LocationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class DriverLocationController {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String GEO_KEY = "places";
    @MessageMapping("/location")
    @SendTo("/topic/driver/location") // <-- THE FIX: Add "/topic" prefix
    public LocationDto sendLocation(LocationDto locationDto, Principal principal){
        if (principal == null || principal.getName() == null || principal.getName().trim().isEmpty()) {
            throw new IllegalStateException("Unauthenticated WebSocket message: principal is missing or empty");
        }

        String userEmail = principal.getName().trim();
        redisTemplate.opsForGeo().add(
                GEO_KEY,
                new Point(locationDto.getLongitude(), locationDto.getLatitude()),
                userEmail
        );
        return new LocationDto(locationDto.getLongitude(), locationDto.getLatitude());
    }




}
