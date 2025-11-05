package com.example.interviewsitebackend.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    // roomId -> list of players (map: name -> role)
    private final Map<String, LinkedHashMap<String,String>> rooms = new ConcurrentHashMap<>();

    @PostMapping("/{roomId}/join")
    public ResponseEntity<Map<String, Object>> joinRoom(@PathVariable String roomId,
                                                        @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String role = body.getOrDefault("role", "CANDIDATE").toUpperCase(); // "recruiter" or "candidate"
        rooms.putIfAbsent(roomId, new LinkedHashMap<>());
        rooms.get(roomId).put(name, role);

        Map<String, Object> resp = new HashMap<>();
        resp.put("participants", rooms.get(roomId));
        resp.put("count", rooms.get(roomId).size());

        // Determine if recruiter is present
        boolean hasRecruiter = rooms.get(roomId).values().stream().anyMatch(r -> r.equals("RECRUITER"));
        resp.put("hasRecruiter", hasRecruiter);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable String roomId, @RequestBody Map<String,String> body) {
        String name = body.get("name");
        if (rooms.containsKey(roomId)) {
            rooms.get(roomId).remove(name);
            if (rooms.get(roomId).isEmpty()) rooms.remove(roomId);
        }
        return ResponseEntity.ok().build();
    }
}
