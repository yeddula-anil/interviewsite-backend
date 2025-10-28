package com.example.interviewsitebackend.dto;


import lombok.Data;

@Data
public class CreateExperienceRequest {
    private String authorId;       // optional
    private String authorName;
    private String authorAvatar;   // url
    private String company;
    private String role;
    private String description;
}

