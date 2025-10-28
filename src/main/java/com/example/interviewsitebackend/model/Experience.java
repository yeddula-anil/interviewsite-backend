package com.example.interviewsitebackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "experiences")
public class Experience {
    @Id
    private String id;

    private String authorId;
    private String authorName;
    private String authorAvatar;
    private String company;
    private String role;
    private String description;
    private Instant createdAt;

    // Set of user IDs who liked this experience
    @Builder.Default
    private Set<String> likedBy = new HashSet<>();

    public int getLikes() {
        return likedBy.size();
    }
}
