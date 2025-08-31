//package com.uber.config;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.ChannelRegistration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@EnableWebSocketMessageBroker
//@RequiredArgsConstructor
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    private final JwtChannelInterceptor jwtChannelInterceptor;
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws") // WebSocket endpoint
//                .setAllowedOriginPatterns("*")
//                .withSockJS(); // fallback for old browsers
//    }
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/topic"); // enable simple broker
//        registry.setApplicationDestinationPrefixes("/app"); // prefix for controller methods
//    }
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(jwtChannelInterceptor);
//    }
//}
//
//
package com.uber.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket endpoint
                .setAllowedOriginPatterns("*")
                .withSockJS(); // fallback for old browsers
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // --- THIS IS THE UPDATED PART ---
        // Broker for public topics ("/topic") and private user queues ("/user")
        registry.enableSimpleBroker("/topic", "/user");

        // Prefix for all controller methods (e.g., @MessageMapping("/location"))
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations, enabling private messages
        registry.setUserDestinationPrefix("/user");
        // --- END OF UPDATE ---
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}