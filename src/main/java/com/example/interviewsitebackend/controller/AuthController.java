package com.example.interviewsitebackend.controller;

import com.example.interviewsitebackend.dto.*;
import com.example.interviewsitebackend.model.User;
import com.example.interviewsitebackend.service.AuthService;
import com.example.interviewsitebackend.service.JwtService;
import com.example.interviewsitebackend.service.MeetingService;
import com.example.interviewsitebackend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor


public class AuthController {

    private final AuthService authService;
    private final MeetingService meetingService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request, @RequestParam(required = false) String token, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        if(token!=null){
            meetingService.handlePostSignup(token);
        }
        setAuthCookies(response, authResponse);
        return authResponse;

    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request,HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setAuthCookies(response, authResponse);
        return authResponse;
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        clearCookies(response);
        return ResponseEntity.ok("Logged out successfully");
    }
    private void setAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
        boolean isProduction = true; // set false if running backend locally

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(isProduction) // ✅ must be true for deployed HTTPS backend
                .path("/")
                .maxAge(15 * 60)
                .sameSite("None") // ✅ required for cross-origin cookies
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        System.out.println("✅ Cookies set (secure=" + isProduction + ")");
    }


    private void clearCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.example.interviewsitebackend.model.User user) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("refresh called");
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return ResponseEntity.status(401).body("No cookies found");

        String refreshToken = Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null || !jwtService.isTokenValid(refreshToken))
            return ResponseEntity.status(401).body("Invalid refresh token");

        String username = jwtService.extractUsername(refreshToken);
        Optional<User> optionalUser = userService.findByEmail(username);
        if (optionalUser.isEmpty()) return ResponseEntity.status(401).body("User not found");

        User user = optionalUser.get();

        String newAccessToken = jwtService.generateAccessToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        // ✅ Set access token cookie
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtService.getAccessExpiration() / 1000)
                .sameSite("None") // "Lax" is safer for local dev
                .build();

        // ✅ Set refresh token cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtService.getRefreshExpiration() / 1000)
                .sameSite("None")
                .build();
        System.out.println("Setting access cookie: " + accessCookie.getValue());
        System.out.println("Setting refresh cookie: " + refreshCookie.getValue());

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }



}
