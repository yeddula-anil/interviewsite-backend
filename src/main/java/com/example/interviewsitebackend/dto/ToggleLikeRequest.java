package com.example.interviewsitebackend.dto;



import lombok.Data;

@Data
public class ToggleLikeRequest {
    private String userId; // optional: track per-user likes later
}

