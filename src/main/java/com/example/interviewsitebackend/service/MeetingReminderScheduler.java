package com.example.interviewsitebackend.service;


import com.example.interviewsitebackend.model.Meeting;
import com.example.interviewsitebackend.model.Reminder;
import com.example.interviewsitebackend.repo.MeetingRepository;
import com.example.interviewsitebackend.repo.ReminderRepository;
import com.example.interviewsitebackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingReminderScheduler {

    private final MeetingRepository meetingRepository;
    private final ReminderRepository reminderRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void sendMeetingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        // Get today’s date
        String today = now.toLocalDate().toString();
        List<Meeting> meetings = meetingRepository.findMeetingsByDate(today);

        for (Meeting meeting : meetings) {
            try {
                // Parse stored date and time into LocalDateTime
                LocalDate meetingDate = LocalDate.parse(meeting.getDate(), DATE_FORMAT);
                LocalTime meetingTime = LocalTime.parse(meeting.getTime(), TIME_FORMAT);
                LocalDateTime scheduledTime = LocalDateTime.of(meetingDate, meetingTime);

                // Skip if reminder already sent
                if (reminderRepository.findByMeetingId(meeting.getId()).isPresent()) continue;

                // Check if it's within next 1 hour
                if (scheduledTime.isAfter(now) && scheduledTime.isBefore(oneHourLater)) {
                    List<String> recipients = Arrays.asList(
                            meeting.getCandidateEmail(),
                            meeting.getRecruiterEmail()
                    );

                    for (String email : recipients) {
                        emailService.sendMeetingReminder(
                                email,
                                meeting.getRole(),
                                meeting.getDate(),
                                meeting.getTime(),
                                meeting.getCompanyName()
                        );
                    }

                    // Log reminder
                    Reminder reminder = Reminder.builder()
                            .meetingId(meeting.getId())
                            .emails(recipients)
                            .sentAt(LocalDateTime.now())
                            .status("SENT")
                            .build();

                    reminderRepository.save(reminder);
                    System.out.println("✅ Reminder sent for meeting ID: " + meeting.getId());
                }
            } catch (Exception e) {
                Reminder failed = Reminder.builder()
                        .meetingId(meeting.getId())
                        .sentAt(LocalDateTime.now())
                        .status("FAILED")
                        .build();
                reminderRepository.save(failed);
                System.err.println("⚠️ Failed to send reminder for " + meeting.getId() + ": " + e.getMessage());
            }
        }
    }
}

