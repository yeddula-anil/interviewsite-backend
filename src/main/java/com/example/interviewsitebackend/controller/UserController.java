package com.example.interviewsitebackend.controller;


import com.example.interviewsitebackend.dto.UpdateProfileRequest;
import com.example.interviewsitebackend.model.User;
import com.example.interviewsitebackend.service.MeetingService;
//import com.example.interviewsitebackend.service.UserService;
import com.example.interviewsitebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

//    private final UserService userService;
    private final MeetingService meetingService;
    private final UserService userService;


    @PutMapping("/update/{userId}")
    public User updateProfile(
            @PathVariable String userId,
            @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(userId, request);
    }

    // Get profile
    @GetMapping("/{userId}")
    public User getProfile(@PathVariable String userId) {
        return userService.getProfile(userId);
    }
}

