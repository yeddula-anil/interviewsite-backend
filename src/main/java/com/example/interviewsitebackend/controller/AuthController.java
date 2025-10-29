package com.example.interviewsitebackend.controller;

import com.example.interviewsitebackend.dto.*;
import com.example.interviewsitebackend.model.User;
import com.example.interviewsitebackend.service.AuthService;
import com.example.interviewsitebackend.service.JwtService;
import com.example.interviewsitebackend.service.MeetingService;
import com.example.interviewsitebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MeetingService meetingService;
    private final JwtService jwtService;
    private final UserService userService;

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request,
                                      @RequestParam(required = false) String token) {
        AuthResponse authResponse = authService.register(request);
        if (token != null) {
            meetingService.handlePostSignup(token);
        }
        return ResponseEntity.ok(authResponse);
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        System.out.println("login called");
        try {
            AuthResponse authResponse = authService.login(request);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ---------------- LOGOUT ----------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // No cookies to clear, frontend just removes from localStorage
        return ResponseEntity.ok("Logged out successfully");
    }

    // ---------------- CURRENT USER ----------------
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        System.out.println("getCurrentUser called");
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }
}
