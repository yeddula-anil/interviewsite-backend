package com.example.interviewsitebackend.controller;

import com.example.interviewsitebackend.model.Evaluation;
import com.example.interviewsitebackend.repo.EvaluationRepository;
import com.example.interviewsitebackend.service.WhisperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SpeechToTextController {

    private final WhisperService whisperService;
    private final EvaluationRepository evaluationRepository;

    /**
     * ✅ Takes JSON body with { meetingId, role, timestamp, audioBase64 }
     * Converts to text → adds timestamped + role-labeled transcript
     */
    @PostMapping("/audio")
    public ResponseEntity<String> handleAudioUpload(@RequestBody Map<String, String> body) {
        try {
            String meetingId = body.get("meetingId");
            String role = body.get("role");
            String audioBase64 = body.get("audioBase64");
            String timestampStr = body.get("timestamp");

            if (meetingId == null || role == null || audioBase64 == null || timestampStr == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            long timestamp = Long.parseLong(timestampStr);
            LocalDateTime recordedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault()
            );

            System.out.printf("[SpeechToText] Received chunk (%s) at %s%n", role, recordedAt);

            // 1️⃣ Decode Base64 audio to bytes
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);

            // 2️⃣ Transcribe audio → text
            String text = whisperService.transcribe(audioBytes);
            if (text == null || text.isBlank()) {
                return ResponseEntity.ok("No speech detected for " + role);
            }

            // 3️⃣ Find or create Evaluation entry
            Evaluation evaluation = evaluationRepository.findById(meetingId)
                    .orElse(Evaluation.builder()
                            .meetingId(meetingId)
                            .fullTranscript("")
                            .lastUpdated(LocalDateTime.now())
                            .build());

            // 4️⃣ Append with chronological ordering
            String existing = evaluation.getFullTranscript() == null ? "" : evaluation.getFullTranscript();
            String newLine = String.format("[%s | %s] %s%n",
                    role.toUpperCase(),
                    recordedAt.toLocalTime().withNano(0),
                    text.trim()
            );

            // Combine & sort by time if multiple lines
            String combined = (existing + newLine)
                    .lines()
                    .sorted((a, b) -> {
                        try {
                            String timeA = a.split("\\|")[1].trim().replace("]", "");
                            String timeB = b.split("\\|")[1].trim().replace("]", "");
                            return timeA.compareTo(timeB);
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .reduce("", (acc, line) -> acc + line + "\n");

            evaluation.setFullTranscript(combined);
            evaluation.setLastUpdated(LocalDateTime.now());

            // ✅ 5️⃣ Save updated transcript
            evaluationRepository.save(evaluation);

            System.out.println("[SpeechToText] ✅ Added transcript line for " + role);
            return ResponseEntity.ok("Transcript updated for " + role);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
