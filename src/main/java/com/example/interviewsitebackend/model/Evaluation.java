package com.example.interviewsitebackend.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "evaluation")
public class Evaluation {

    @Id
    private String meetingId; // unique meeting ID for each interview

    private String fullTranscript; // combined conversation text
    private String feedback;       // AI evaluation result
    private Double score;          // numeric score (0–10)
    private LocalDateTime lastUpdated;
}

