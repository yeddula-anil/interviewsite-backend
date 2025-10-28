package com.example.interviewsitebackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "meeting_invite_tokens")
public class MeetingInviteToken {

    @Id
    private String id;
    private String token;
    private String candidateEmail;
    private String recruiterName;
    private String companyName;
    private String companyLogoUrl;
    private String role;
    private String date;
    private String time;
    private boolean accepted;
    private String meetingId; // optional if needed
}
