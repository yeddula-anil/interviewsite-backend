package com.example.interviewsitebackend.controller;

import com.example.interviewsitebackend.dto.SignalMessage;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    public SignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/signal/{roomId}")
    public void handleSignal(@DestinationVariable String roomId, @Payload SignalMessage message) {

        // ✅ FIX: Removed the toUpperCase() conversion.
        // The client-side (JavaScript) almost certainly expects lowercase
        // types like "offer", "answer", and "candidate".
        // Changing the case here breaks the WebRTC negotiation.
        /*
        if (message.getType() != null) {
            message.setType(message.getType().toUpperCase());
        }
        */

        System.out.printf("Broadcasting signal in room=%s from=%s type=%s%n", roomId, message.getSender(), message.getType());

        // Broadcast the message to all other clients in the room
        messagingTemplate.convertAndSend("/topic/signal/" + roomId, message);
    }

}