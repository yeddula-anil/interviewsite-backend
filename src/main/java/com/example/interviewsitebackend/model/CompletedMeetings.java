package com.example.interviewsitebackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "completed_meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedMeetings {

    @Id
    private String id;

    private String meetingId;
    private String candidateEmail;
    private String recruiterEmail;
    private String role;
    private String thumbnail;
    private String time;
}
