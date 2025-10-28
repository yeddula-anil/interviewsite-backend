package com.example.interviewsitebackend.service;

import com.example.interviewsitebackend.dto.MeetingTimingRequest;
import com.example.interviewsitebackend.model.Meeting;
import com.example.interviewsitebackend.model.MeetingInviteToken;
import com.example.interviewsitebackend.model.User;
import com.example.interviewsitebackend.repo.MeetingInviteTokenRepository;
import com.example.interviewsitebackend.repo.MeetingRepository;
import com.example.interviewsitebackend.repo.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepo;
    private final MeetingInviteTokenRepository inviteRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;

    // Schedule meeting: only saves token
    public void scheduleMeeting(Meeting meeting) {
        String token = UUID.randomUUID().toString();
//        this is to be deleted


        MeetingInviteToken invite = MeetingInviteToken.builder()
                .token(token)
                .candidateEmail(meeting.getCandidateEmail())
                .recruiterName(meeting.getRecruiterName())
                .companyName(meeting.getCompanyName())
                .companyLogoUrl(meeting.getCompanyLogoUrl())
                .role(meeting.getRole())
                .date(meeting.getDate())
                .time(meeting.getTime())
                .accepted(false)
                .build();

        inviteRepo.save(invite);
        meetingRepo.save(meeting);
        System.out.println("meeting scheduled");
        sendInviteEmail(invite);
    }

    private void sendInviteEmail(MeetingInviteToken invite) {
        String acceptUrl = "http://localhost:3000/signup?token=" + invite.getToken(); // frontend URL

        String html = """
            <div style="font-family:Arial,sans-serif">
                <h2>Interview Invitation from %s</h2>
                <img src="%s" alt="Company Logo" width="100"/>
                <p><strong>Company:</strong> %s</p>
                <p><strong>Role:</strong> %s</p>
                <p><strong>Date:</strong> %s</p>
                <p><strong>Time:</strong> %s</p>
                <a href="%s" style="background:#4CAF50;color:white;padding:10px 20px;
                   text-decoration:none;border-radius:5px;">Accept Meeting</a>
            </div>
        """.formatted(
                invite.getRecruiterName(),
                invite.getCompanyLogoUrl(),
                invite.getCompanyName(),
                invite.getRole(),
                invite.getDate(),
                invite.getTime(),
                acceptUrl
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(invite.getCandidateEmail());
            helper.setSubject("Interview Invite - " + invite.getCompanyName());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    // Handle candidate clicking Accept link
    public String handleMeetingAcceptance(String token) {
        Optional<MeetingInviteToken> tokenOpt = inviteRepo.findByToken(token);
        if (tokenOpt.isEmpty()) throw new RuntimeException("Invalid token");

        MeetingInviteToken invite = tokenOpt.get();
        Optional<User> userOpt = userRepo.findByEmail(invite.getCandidateEmail());

        if (userOpt.isPresent()) {
            // User exists → store meeting
            storeMeeting(invite);
            invite.setAccepted(true);
            inviteRepo.save(invite);
            return "accepted";
        } else {
            return "redirect_signup";
        }
    }

    // Call this after signup if token exists
    public void handlePostSignup(String token) {
        if (token != null) {
            Optional<MeetingInviteToken> tokenOpt = inviteRepo.findByToken(token);
            if (tokenOpt.isPresent() && !tokenOpt.get().isAccepted()) {
                MeetingInviteToken invite = tokenOpt.get();
                storeMeeting(invite);
                invite.setAccepted(true);
                inviteRepo.save(invite);
            }
        }
    }

    private Meeting storeMeeting(MeetingInviteToken invite) {
        Meeting meeting = Meeting.builder()
                .recruiterName(invite.getRecruiterName())
                .candidateEmail(invite.getCandidateEmail())
                .companyName(invite.getCompanyName())
                .companyLogoUrl(invite.getCompanyLogoUrl())
                .role(invite.getRole())
                .date(invite.getDate())
                .time(invite.getTime())
                .accepted(true)
                .build();

        Meeting saved = meetingRepo.save(meeting);
        sendAcceptanceConfirmationEmail(saved);
        return saved;
    }

    private void sendAcceptanceConfirmationEmail(Meeting meeting) {
        String html = """
            <div style="font-family:Arial,sans-serif">
                <h2>Thank You for Accepting!</h2>
                <p>Hi,</p>
                <p>Thank you for accepting the interview invitation for <strong>%s</strong> role at <strong>%s</strong>.</p>
                <p>All the best for your interview!</p>
                <p>Regards,<br/>%s Team</p>
            </div>
        """.formatted(meeting.getRole(), meeting.getCompanyName(), meeting.getRecruiterName());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(meeting.getCandidateEmail());
            helper.setSubject("Interview Confirmation - " + meeting.getCompanyName());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error sending confirmation email: " + e.getMessage());
        }
    }
    public List<Meeting> getMeetingsByCandidateEmail(String candidateEmail) {
        return meetingRepo.findByCandidateEmail(candidateEmail);
    }

    public List<Meeting> getMeetingsByRecruiterEmail(String recruiterEmail) {
        return meetingRepo.findByRecruiterEmail(recruiterEmail);
    }
    public Meeting writeReview(String meetingId, String review) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        meeting.setCandidateComments(review);
        meeting.setMarkedAt(String.valueOf(System.currentTimeMillis()));

        return meetingRepo.save(meeting);
    }
    public Meeting assignScore(String meetingId, Integer score) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        meeting.setCandidateMarks(score);
        meeting.setMarkedAt(LocalDate.now().toString());

        meeting.setMarkedAt(String.valueOf(System.currentTimeMillis()));

        return meetingRepo.save(meeting);
    }
    public Meeting updateMeetingTiming(String id, MeetingTimingRequest request) {
        Meeting meeting = meetingRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found"));

        meeting.setDate(request.getDate());
        meeting.setTime(request.getTime());

        return meetingRepo.save(meeting);
    }
    public void sendTimingUpdateEmail(Meeting meeting) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(meeting.getCandidateEmail());
        helper.setSubject("Interview Timing Updated");
        helper.setText(
                "<p>Dear Candidate,</p>" +
                        "<p>Your interview timing has been updated:</p>" +
                        "<ul>" +
                        "<li>Role: " + meeting.getRole() + "</li>" +
                        "<li>Date: " + meeting.getDate() + "</li>" +
                        "<li>Time: " + meeting.getTime() + "</li>" +
                        "</ul>" +
                        "<p>Thank you.</p>",
                true // Enable HTML
        );

        mailSender.send(message);
    }
    public Meeting updateCandidateResumeUrl(String meetingId, String resumeUrl) {
        Optional<Meeting> optionalMeeting = meetingRepo.findById(meetingId);
        if (!optionalMeeting.isPresent()) {
            throw new RuntimeException("Meeting not found with id: " + meetingId);
        }

        Meeting meeting = optionalMeeting.get();
        meeting.setCandidateResumeUrl(resumeUrl);
        return meetingRepo.save(meeting);
    }
    public Meeting deleteMeeting(String id) {
        Optional<Meeting> optionalMeeting = meetingRepo.findById(id);
        if (!optionalMeeting.isPresent()) {
            throw new RuntimeException("Meeting not found with id: " + id);
        }
        Meeting meeting = optionalMeeting.get();
        meetingRepo.delete(meeting);
        System.out.println("Meeting with id " + id + " has been deleted");
        return meeting;
    }

    // Restore meeting (undo)
    public Meeting saveMeeting(Meeting meeting) {
        // Ensure ID is not null to preserve original meetingS
        System.out.println("Meeting with id " + meeting.getId() + " has been saved");
        return meetingRepo.save(meeting);
    }


}
