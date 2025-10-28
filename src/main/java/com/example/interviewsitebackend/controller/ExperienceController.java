package com.example.interviewsitebackend.controller;

import com.example.interviewsitebackend.dto.CreateExperienceRequest;
import com.example.interviewsitebackend.dto.ToggleLikeRequest;
import com.example.interviewsitebackend.model.Experience;
import com.example.interviewsitebackend.service.ExperienceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experiences")
@CrossOrigin(origins = "*")
public class ExperienceController {

    private final ExperienceService service;

    public ExperienceController(ExperienceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Experience> create(@RequestBody CreateExperienceRequest req) {
        Experience saved = service.createExperience(req);
        System.out.println("experience created");
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Experience>> list(
            @RequestParam(value = "company", required = false) String company,
            @RequestParam(value = "role", required = false) String role
    ) {
        System.out.println("experience list called");
        return ResponseEntity.ok(service.listExperiences(company, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        System.out.println("experience get called");
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        System.out.println("experience delete called");
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Like/unlike toggle
    @PutMapping("/{id}/like")
    public ResponseEntity<Experience> toggleLike(
            @PathVariable String id,
            @RequestBody ToggleLikeRequest request
            ) {
        System.out.println("experience toggle-like called");

        Experience updated = service.toggleLike(id, request.getUserId());
        return ResponseEntity.ok(updated);
    }
}
