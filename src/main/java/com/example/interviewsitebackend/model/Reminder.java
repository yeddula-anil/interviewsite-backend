package com.example.interviewsitebackend.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    private String id;

    private String meetingId;
    private List<String> emails;      // recipients (recruiter + candidate)
    private LocalDateTime sentAt;
    private String status;            // SENT or FAILED
}

