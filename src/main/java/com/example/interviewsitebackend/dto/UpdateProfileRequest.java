package com.example.interviewsitebackend.dto;


import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String profilePicUrl;
    private String bannerUrl; // will be null for candidates
}

