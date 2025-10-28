package com.example.interviewsitebackend.controller;


import com.example.interviewsitebackend.dto.SignalMessage;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class SignalingController {

    // Clients send to /app/signal/{roomId} — this method will broadcast to /topic/signal/{roomId}
    @MessageMapping("/signal/{roomId}")
    @SendTo("/topic/signal/{roomId}")
    public SignalMessage signal(@DestinationVariable String roomId, @Payload SignalMessage message) {
        // optional: validate message, sanitize, log
        System.out.println("room=" + roomId + " from=" + message.getSender() + " type=" + message.getType());
        return message; // broadcast to everyone subscribed to /topic/signal/{roomId}
    }
}
