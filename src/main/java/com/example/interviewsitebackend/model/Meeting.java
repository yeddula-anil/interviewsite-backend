package com.example.interviewsitebackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {

    @Id
    private String id;

    private String recruiterName;
    private String recruiterEmail;
    private String candidateEmail;
    private String companyName;
    private String companyLogoUrl;
    private String role;
    private String date;
    private String time;

    private boolean accepted;        // already exists
    private boolean completed;       // mark interview as completed

    private Integer candidateMarks;  // already exists
    private String candidateComments;// already exists

    private String markedBy;         // recruiter/admin who marked completed
    private String markedAt;         // timestamp

    private String candidateResumeUrl; // new: store resume file URL

    private String candidateId;
    private String recruiterId;
}
