package com.example.interviewsitebackend.service;

import com.example.interviewsitebackend.dto.CreateExperienceRequest;
import com.example.interviewsitebackend.model.Experience;
import com.example.interviewsitebackend.repo.ExperienceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ExperienceService {

    private final ExperienceRepository repo;

    public ExperienceService(ExperienceRepository repo) {
        this.repo = repo;
    }

    public Experience createExperience(CreateExperienceRequest req) {
        Experience exp = Experience.builder()
                .authorId(req.getAuthorId())
                .authorName(req.getAuthorName() != null ? req.getAuthorName() : "Anonymous")
                .authorAvatar(req.getAuthorAvatar())
                .company(req.getCompany())
                .role(req.getRole())
                .description(req.getDescription())
                .createdAt(Instant.now())
                .build();
        return repo.save(exp);
    }

    public List<Experience> listExperiences(String companyFilter, String roleFilter) {
        List<Experience> all = repo.findAll();
        all.sort(Comparator.comparing(Experience::getCreatedAt).reversed());

        return all.stream()
                .filter(e -> (companyFilter == null || e.getCompany().toLowerCase().contains(companyFilter.toLowerCase())) &&
                        (roleFilter == null || e.getRole().toLowerCase().contains(roleFilter.toLowerCase())))
                .toList();
    }

    public Optional<Experience> getById(String id) {
        return repo.findById(id);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

    // ✅ Like toggle logic
    public Experience toggleLike(String id, String userId) {
        Experience e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Experience not found"));

        if (e.getLikedBy().contains(userId)) {
            // User already liked → remove like
            e.getLikedBy().remove(userId);
        } else {
            // User hasn't liked yet → add like
            e.getLikedBy().add(userId);
        }

        return repo.save(e);
    }
}
