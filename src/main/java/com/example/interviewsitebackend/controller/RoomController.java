package com.example.interviewsitebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    // roomId -> map of participants (name -> role)
    private final Map<String, LinkedHashMap<String, String>> rooms = new ConcurrentHashMap<>();

    @PostMapping("/{roomId}/join")
    public ResponseEntity<Map<String, Object>> joinRoom(@PathVariable String roomId,
                                                        @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String role = body.getOrDefault("role", "CANDIDATE").toUpperCase(); // "RECRUITER" or "CANDIDATE"

        // Create room if it doesn't exist
        rooms.putIfAbsent(roomId, new LinkedHashMap<>());

        // ✅ Prevent duplicate joins (don’t re-add same user)
        LinkedHashMap<String, String> participants = rooms.get(roomId);
        participants.put(name, role);

        // ✅ Compute recruiter presence correctly
        boolean hasRecruiter = participants.values().stream().anyMatch(r -> r.equals("RECRUITER"));

        // ✅ Prepare response
        Map<String, Object> resp = new HashMap<>();
        resp.put("participants", participants);
        resp.put("count", participants.size());
        resp.put("hasRecruiter", hasRecruiter);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable String roomId,
                                          @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (rooms.containsKey(roomId)) {
            rooms.get(roomId).remove(name);

            // If room is empty, remove it entirely
            if (rooms.get(roomId).isEmpty()) {
                rooms.remove(roomId);
            }
        }
        return ResponseEntity.ok().build();
    }

    // ✅ Optional: GET endpoint to debug active rooms
    @GetMapping
    public ResponseEntity<Map<String, LinkedHashMap<String, String>>> getAllRooms() {
        return ResponseEntity.ok(rooms);
    }
}
