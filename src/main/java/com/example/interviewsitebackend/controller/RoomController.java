package com.example.interviewsitebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    // roomId -> map of participants (name -> role)
    // ✅ This outer map is thread-safe
    private final Map<String, LinkedHashMap<String, String>> rooms = new ConcurrentHashMap<>();

    @PostMapping("/{roomId}/join")
    public ResponseEntity<Map<String, Object>> joinRoom(@PathVariable String roomId,
                                                        @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String role = body.getOrDefault("role", "participant").toLowerCase();

        rooms.putIfAbsent(roomId, new LinkedHashMap<>());
        LinkedHashMap<String, String> participants = rooms.get(roomId);

        boolean isOfferer;
        synchronized (participants) {
            participants.put(name, role);
            // The first user to join the room becomes the offerer
            isOfferer = (participants.size() == 1);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("participants", new LinkedHashMap<>(participants));
        resp.put("count", participants.size());
        resp.put("isOfferer", isOfferer);

        return ResponseEntity.ok(resp);
    }



    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable String roomId,
                                          @RequestBody Map<String, String> body) {
        String name = body.get("name");
        LinkedHashMap<String, String> participants = rooms.get(roomId); // Get the map

        if (participants != null) {
            boolean wasEmpty = false;

            // ✅ FIX: Lock the specific room's participant list for modification
            synchronized (participants) {
                participants.remove(name);
                wasEmpty = participants.isEmpty();
            }

            // If it's empty, we need to remove it from the main 'rooms' map.
            // This requires a lock on the outer 'rooms' map.
            if (wasEmpty) {
                synchronized (rooms) {
                    // ✅ Re-check emptiness *inside the lock*
                    // in case someone joined between the 'participants' lock and the 'rooms' lock.
                    if (participants.isEmpty()) {
                        rooms.remove(roomId);
                    }
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Map<String, LinkedHashMap<String, String>>> getAllRooms() {
        // This is okay for debugging, but for a production-safe view,
        // you would need to lock and deep-copy all rooms.
        return ResponseEntity.ok(rooms);
    }
}