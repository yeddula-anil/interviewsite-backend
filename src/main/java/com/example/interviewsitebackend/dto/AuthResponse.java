package com.example.interviewsitebackend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String username;
    private String email;
    private String accessToken;
    private String refreshToken;
}

