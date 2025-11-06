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
        String role = body.getOrDefault("role", "CANDIDATE").toUpperCase(); // "RECRUITER" or "CANDIDATE"

        // Create room if it doesn't exist (this is thread-safe)
        // ⚠️ We MUST NOT use Collections.synchronizedMap here, as putIfAbsent will
        // create a *new* LinkedHashMap every time, causing a race condition.
        // Instead, we create it and lock on the 'participants' object below.
        rooms.putIfAbsent(roomId, new LinkedHashMap<>());

        // Get the specific map for this room
        LinkedHashMap<String, String> participants = rooms.get(roomId);

        Map<String, Object> resp = new HashMap<>();
        Map<String, String> participantsCopy;
        boolean hasRecruiter;
        int count;

        // ✅ FIX: Lock the 'participants' map before modifying or reading it.
        // This prevents race conditions if two users join at the same time.
        synchronized (participants) {
            participants.put(name, role);
            // Create a copy *inside the lock* for a consistent snapshot
            participantsCopy = new LinkedHashMap<>(participants);
        }

        // Do CPU-intensive work (like streams) *outside* the lock
        hasRecruiter = participantsCopy.values().stream().anyMatch(r -> r.equals("RECRUITER"));
        count = participantsCopy.size();

        resp.put("participants", participantsCopy);
        resp.put("count", count);
        resp.put("hasRecruiter", hasRecruiter);

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