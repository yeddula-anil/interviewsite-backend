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
        System.out.printf("room=%s from=%s type=%s%n", roomId, message.getSender(), message.getType());
        messagingTemplate.convertAndSend("/topic/signal/" + roomId, message);
    }
}
