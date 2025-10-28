package com.example.interviewsitebackend.controller;

import com.example.interviewsitebackend.dto.MeetingTimingRequest;
import com.example.interviewsitebackend.dto.ResumeRequest;
import com.example.interviewsitebackend.dto.Review;
import com.example.interviewsitebackend.dto.Score;
import com.example.interviewsitebackend.model.Meeting;
import com.example.interviewsitebackend.repo.MeetingRepository;
import com.example.interviewsitebackend.service.MeetingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingRepository meetingRepository;

    // Schedule meeting (recruiter)
    @PostMapping("/schedule")
    public ResponseEntity<String> scheduleMeeting(@RequestBody Meeting meeting) {
        meetingService.scheduleMeeting(meeting);
        return ResponseEntity.ok("Meeting invite sent successfully!");
    }

    // Accept link clicked by candidate
    @GetMapping("/accept")
    public ResponseEntity<String> acceptMeeting(@RequestParam String token) {
        String result = meetingService.handleMeetingAcceptance(token);
        if (result.equals("accepted")) {
            return ResponseEntity.ok("Meeting accepted successfully!");
        } else {
            // Redirect frontend to signup page
            return ResponseEntity.status(302)
                    .header("Location", "http://localhost:3000/signup?token=" + token)
                    .build();
        }
    }
    @GetMapping("/candidate/{email}")
    public ResponseEntity<List<Meeting>> getMeetingsByCandidateEmail(@PathVariable String email) {
        List<Meeting> meetings = meetingService.getMeetingsByCandidateEmail(email);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/recruiter/{email}")
    public ResponseEntity<List<Meeting>> getMeetingsByRecruiterEmail(@PathVariable String email) {
        System.out.println("schedule called");
        List<Meeting> meetings = meetingService.getMeetingsByRecruiterEmail(email);
        return ResponseEntity.ok(meetings);
    }
    @PutMapping("/{id}/complete")
    public ResponseEntity<String> markMeetingAsCompleted(@PathVariable String id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id " + id));

        meeting.setCompleted(true);
        meetingRepository.save(meeting);

        return ResponseEntity.ok("Meeting marked as completed");
    }
    @PutMapping("/{meetingId}/score")
    public ResponseEntity<?> assignScore(
            @PathVariable String meetingId,
            @RequestBody Score score // just the integer
    ) {
        Meeting updatedMeeting = meetingService.assignScore(meetingId, score.getScore());
        return ResponseEntity.ok("Score assigned successfully");
    }
    @PutMapping("/{meetingId}/review")
    public ResponseEntity<?> writeReview(
            @PathVariable String meetingId,
            @RequestBody Review review // just the string
    ) {
        Meeting updatedMeeting = meetingService.writeReview(meetingId, review.getReview());
        return ResponseEntity.ok("Review submitted successfully");
    }
    @PutMapping("/{id}/update-timing")
    public ResponseEntity<?> updateTiming(
            @PathVariable String id,
            @RequestBody MeetingTimingRequest request) {
        System.out.println("updateTiming called");

        try {
            Meeting updatedMeeting = meetingService.updateMeetingTiming(id,request);
            meetingService.sendTimingUpdateEmail(updatedMeeting);
            return ResponseEntity.ok(updatedMeeting);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Meeting not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update meeting");
        }
    }
    @PutMapping("/{id}/resume")
    public ResponseEntity<?> updateCandidateResume(
            @PathVariable("id") String meetingId,
            @RequestBody ResumeRequest request) {
        System.out.println("updateCandidateResume called");
        try {
            Meeting updatedMeeting = meetingService.updateCandidateResumeUrl(meetingId, request.getResumeUrl());
            return ResponseEntity.ok(updatedMeeting);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating resume URL: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeMeeting(@PathVariable String id) {
        try {
            Meeting deleted = meetingService.deleteMeeting(id);
            return ResponseEntity.ok(deleted);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error deleting meeting: " + e.getMessage());
        }
    }

    // Restore meeting (undo)
    @PostMapping
    public ResponseEntity<?> restoreMeeting(@RequestBody Meeting meeting) {
        try {
            Meeting restored = meetingService.saveMeeting(meeting);
            return ResponseEntity.ok(restored);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error restoring meeting: " + e.getMessage());
        }
    }






}
