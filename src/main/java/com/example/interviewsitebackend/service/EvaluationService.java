package com.example.interviewsitebackend.service;


import com.example.interviewsitebackend.model.Evaluation;
import com.example.interviewsitebackend.repo.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;

    public Map<String, Object> evaluateMeeting(String meetingId) {
        Evaluation eval = evaluationRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found: " + meetingId));

        String transcript = eval.getFullTranscript();
        if (transcript == null || transcript.isEmpty()) {
            throw new RuntimeException("No transcript found for meeting: " + meetingId);
        }

        String feedback = callOpenRouter(transcript);
        double score = extractScore(feedback);

        eval.setFeedback(feedback);
        eval.setScore(score);
        eval.setLastUpdated(java.time.LocalDateTime.now());
        evaluationRepository.save(eval);

        Map<String, Object> response = new HashMap<>();
        response.put("meetingId", meetingId);
        response.put("score", score);
        response.put("feedback", feedback);
        return response;
    }

    private String callOpenRouter(String transcript) {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        String url = "https://openrouter.ai/api/v1/chat/completions";

        String body = """
            {
              "model": "meta-llama/llama-3-8b-instruct",
              "messages": [
                {"role": "system", "content": "You are an AI interviewer that evaluates technical interviews and gives a score (0–10) with detailed feedback."},
                {"role": "user", "content": "%s"}
              ]
            }
        """.formatted(transcript);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    private double extractScore(String feedback) {
        Pattern p = Pattern.compile("(\\d+(\\.\\d+)?)\\s*/\\s*10");
        Matcher m = p.matcher(feedback);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return Math.random() * 10; // fallback if no explicit score in response
    }
}

