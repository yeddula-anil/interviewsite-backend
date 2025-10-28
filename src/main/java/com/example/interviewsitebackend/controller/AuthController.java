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

    // ----------------------- REGISTER -----------------------
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request,
                                 @RequestParam(required = false) String token,
                                 HttpServletResponse response,
                                 HttpServletRequest req) {
        AuthResponse authResponse = authService.register(request);
        if (token != null) {
            meetingService.handlePostSignup(token);
        }
        setAuthCookies(req, response, authResponse);
        return authResponse;
    }

    // ----------------------- LOGIN -----------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request,
                                   HttpServletResponse response,
                                   HttpServletRequest req) {
        try {
            AuthResponse authResponse = authService.login(request);
            setAuthCookies(req, response, authResponse);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ----------------------- LOGOUT -----------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        clearCookies(response);
        return ResponseEntity.ok("Logged out successfully");
    }

    // ----------------------- COOKIE SETUP -----------------------
    private void setAuthCookies(HttpServletRequest request,
                                HttpServletResponse response,
                                AuthResponse authResponse) {

        boolean isProduction = !request.getServerName().contains("localhost");

        // ✅ Force SameSite=None and Secure=true for Render HTTPS
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(isProduction) // true in production
                .path("/")
                .sameSite(isProduction ? "None" : "Lax")
                .maxAge(15 * 60)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .sameSite(isProduction ? "None" : "Lax")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        // ⚠️ This header must be added only once per response, not per cookie
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // ✅ Add both cookies properly
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        System.out.println("✅ Cookies set (secure=" + isProduction + ")");
    }

    // ----------------------- CLEAR COOKIES -----------------------
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

    // ----------------------- CURRENT USER -----------------------
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    // ----------------------- REFRESH TOKEN -----------------------
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("refresh called");
        System.out.println("Incoming cookies: " + Arrays.toString(request.getCookies()));

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(401).body("No cookies found");
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        Optional<User> optionalUser = userService.findByEmail(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = optionalUser.get();

        String newAccessToken = jwtService.generateAccessToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(jwtService.getAccessExpiration() / 1000)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(jwtService.getRefreshExpiration() / 1000)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        System.out.println("✅ Tokens refreshed successfully");
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
