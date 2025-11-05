package com.example.interviewsitebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "https://interviewsite-frontend.vercel.app",
                        "https://interviewsite-frontend.onrender.com",
                        "http://localhost:3000"
                )
                .setAllowedOrigins("*") // only keep for testing
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // clients can subscribe to /topic/...
        registry.enableSimpleBroker("/topic");
        // clients send messages to /app/...
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // Allow larger signaling messages (for SDP)
        registry.setMessageSizeLimit(2 * 1024 * 1024);
        registry.setSendBufferSizeLimit(2 * 1024 * 1024);
        registry.setSendTimeLimit(20_000);
    }
}
