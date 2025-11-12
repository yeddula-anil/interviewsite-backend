package com.example.interviewsitebackend.controller;


import com.example.interviewsitebackend.model.CompletedMeetings;
import com.example.interviewsitebackend.model.Meeting;
import com.example.interviewsitebackend.repo.MeetingRepository;
import com.example.interviewsitebackend.repo.CompletedMeetingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/completed-meetings")
@RequiredArgsConstructor
public class CompletedMeetingsController {

    private final MeetingRepository meetingRepository;
    private final CompletedMeetingsRepository completedMeetingsRepository;

    // ✅ Save completed meeting based on meetingId only
    @PostMapping("/{meetingId}")
    public ResponseEntity<String> saveCompletedMeeting(@PathVariable String meetingId) {
        // Fetch meeting details
        Optional<Meeting> optional = meetingRepository.findById(meetingId);
        if(!optional.isPresent()) {
            throw new RuntimeException("meeting not found");
        }
        Meeting meeting=optional.get();

        // Build completed meeting object
        CompletedMeetings completedMeeting = CompletedMeetings.builder()
                .meetingId(meeting.getId())
                .candidateEmail(meeting.getCandidateEmail())
                .role(meeting.getRole())
                .recruiterEmail(meeting.getRecruiterEmail())
                .thumbnail(meeting.getCompanyLogoUrl())
                .time(meeting.getDate())
                .build();

        // Save to completed_meetings collection
        completedMeetingsRepository.save(completedMeeting);

        return ResponseEntity.ok("✅ Meeting marked as completed and stored successfully");
    }
    @GetMapping("/candidate/{email}")
    public ResponseEntity<List<CompletedMeetings>> getMeetingsByCandidateEmail(@PathVariable String email) {
        List<CompletedMeetings> meetings = completedMeetingsRepository.findByCandidateEmail(email);
        if (meetings.isEmpty()) {
            return ResponseEntity.noContent().build(); // HTTP 204 if no meetings found
        }
        return ResponseEntity.ok(meetings);
    }
}

