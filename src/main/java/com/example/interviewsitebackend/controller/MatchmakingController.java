package com.example.interviewsitebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

    private static class User {
        String username;
        String meetingId;

        User(String username, String meetingId) {
            this.username = username;
            this.meetingId = meetingId;
        }
    }

    // Store waiting users by meetingId
    private final Map<String, User> waitingUsers = new ConcurrentHashMap<>();

    /**
     * When a user joins with a meetingId:
     * - If no one is waiting, they become the first (offerer).
     * - If someone is waiting with the same meetingId, they become the answerer.
     */
    @PostMapping("/join")
    public synchronized ResponseEntity<Map<String, Object>> joinQueue(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String meetingId = body.get("meetingId");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username required"));
        }
        if (meetingId == null || meetingId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Meeting ID required"));
        }

        Map<String, Object> response = new HashMap<>();

        // Check if someone is already waiting for this meeting
        User waiting = waitingUsers.get(meetingId);

        if (waiting == null) {
            // No one waiting → this user becomes offerer
            waitingUsers.put(meetingId, new User(username, meetingId));
            System.out.println("🕐 " + username + " is waiting for a partner in meeting " + meetingId);
            response.put("matched", false);
            response.put("isOfferer", true);
            response.put("message", "Waiting for another participant...");
        } else {
            // Found a waiting user → match them
            User offerer = waiting;
            waitingUsers.remove(meetingId);
            System.out.println("🎯 Matched: " + offerer.username + " (offerer) <-> " + username + " (answerer)");

            response.put("matched", true);
            response.put("offerer", offerer.username);
            response.put("answerer", username);
            response.put("isOfferer", false);
            response.put("roomId", meetingId);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/leave")
    public synchronized ResponseEntity<Map<String, String>> leaveQueue(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String meetingId = body.get("meetingId");

        if (meetingId != null) {
            User waiting = waitingUsers.get(meetingId);
            if (waiting != null && waiting.username.equals(username)) {
                waitingUsers.remove(meetingId);
                System.out.println("🚪 Removed " + username + " from waiting queue for " + meetingId);
            }
        }
        return ResponseEntity.ok(Map.of("message", "User removed"));
    }

    @GetMapping("/waiting")
    public ResponseEntity<Map<String, Object>> getWaitingList() {
        return ResponseEntity.ok(Map.of(
                "waitingCount", waitingUsers.size(),
                "waitingUsers", waitingUsers.keySet()
        ));
    }
}
