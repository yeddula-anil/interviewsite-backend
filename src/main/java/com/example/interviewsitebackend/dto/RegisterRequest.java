package com.example.interviewsitebackend.dto;


import com.example.interviewsitebackend.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Role role;
}

