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
                        "http://localhost:3000",
                        "*" // for testing — remove later in production
                )
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    // Optional: allow larger signaling messages
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(2 * 1024 * 1024); // 2MB
        registry.setSendBufferSizeLimit(2 * 1024 * 1024);
        registry.setSendTimeLimit(20000);
    }
}
