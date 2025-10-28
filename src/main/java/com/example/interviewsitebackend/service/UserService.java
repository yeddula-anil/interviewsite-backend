package com.example.interviewsitebackend.service;

import com.example.interviewsitebackend.dto.UpdateProfileRequest;
import com.example.interviewsitebackend.model.User;
import com.example.interviewsitebackend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User updateProfile(String userId, UpdateProfileRequest request) {
        System.out.println("tried to update profile");
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Update all allowed fields
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getProfilePicUrl() != null) user.setProfilePicUrl(request.getProfilePicUrl());
        if (request.getBannerUrl() != null) user.setBannerUrl(request.getBannerUrl());

        // Password is never updated here
        return userRepository.save(user);
    }

    public User getProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> findByUsernameOptional(String username) {
        return userRepository.findByUsername(username);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
