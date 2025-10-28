package com.example.interviewsitebackend.repo;


import com.example.interviewsitebackend.model.Experience;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends MongoRepository<Experience, String> {
    // simple query by company (case-insensitive contains)
    List<Experience> findByCompanyIgnoreCaseContainingAndRoleIgnoreCaseContainingOrderByCreatedAtDesc(
            String company, String role
    );

    // fallback: find all sorted by createdAt desc (can be used in service)
}

