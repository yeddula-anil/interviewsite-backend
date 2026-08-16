package com.example.interviewsitebackend.controller;


import com.example.interviewsitebackend.model.Evaluation;
import com.example.interviewsitebackend.repo.EvaluationRepository;
import com.example.interviewsitebackend.service.EvaluationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationService evaluationService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${deepgram.api.key:}")
    private String deepgramApiKey;

    public EvaluationController(EvaluationRepository evaluationRepository, EvaluationService evaluationService) {
        this.evaluationRepository = evaluationRepository;
        this.evaluationService = evaluationService;
    }

    @PostMapping("/evaluate/{meetingId}")
    public ResponseEntity<?> evaluateMeeting(@PathVariable String meetingId) {
        try {
            Map<String, Object> result = evaluationService.evaluateMeeting(meetingId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<?> getEvaluation(@PathVariable String meetingId) {
        return evaluationRepository.findById(meetingId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Evaluation not found for meeting: " + meetingId)));
    }

    /**
     * 🎧 Accepts 15s mixed audio chunks (multipart/form-data)
     * Converts them to text via Deepgram API
     * Appends the result to the Evaluation document in MongoDB.
     */
    @PostMapping(value = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleAudioChunk(
            @RequestPart("audio") MultipartFile audioFile,
            @RequestParam("meetingId") String meetingId
    ) {
        try {
            System.out.println("🎤 Received audio chunk for meeting: " + meetingId);

            // 1️⃣ Transcribe the audio chunk
            String transcript = transcribeAudio(audioFile);
            if (transcript == null || transcript.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Transcription failed or empty transcript"));
            }

            // 2️⃣ Find or create Evaluation record
            Evaluation evaluation = evaluationRepository.findById(meetingId)
                    .orElse(Evaluation.builder()
                            .meetingId(meetingId)
                            .fullTranscript("")
                            .lastUpdated(LocalDateTime.now())
                            .build());

            // 3️⃣ Append transcript chunk
            evaluation.setFullTranscript(
                    evaluation.getFullTranscript() + "\n" + transcript
            );
            evaluation.setLastUpdated(LocalDateTime.now());
            evaluationRepository.save(evaluation);

            // 4️⃣ Respond with confirmation
            return ResponseEntity.ok(Map.of(
                    "meetingId", meetingId,
                    "transcriptChunk", transcript,
                    "timestamp", LocalDateTime.now().toString()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🧠 Helper: Sends audio bytes to Deepgram for transcription
     */
    private String transcribeAudio(MultipartFile audioFile) throws IOException {
        String url = "https://api.deepgram.com/v1/listen?model=nova-2&smart_format=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/webm"));
        headers.setBearerAuth(deepgramApiKey);

        HttpEntity<byte[]> entity = new HttpEntity<>(audioFile.getBytes(), headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        Map<?, ?> body = response.getBody();

        if (body == null) return null;

        try {
            // Deepgram JSON path: results.channels[0].alternatives[0].transcript
            Map<?, ?> results = (Map<?, ?>) ((List<?>) body.get("results")).get(0);
            Map<?, ?> channel = (Map<?, ?>) ((List<?>) results.get("channels")).get(0);
            Map<?, ?> alt = (Map<?, ?>) ((List<?>) channel.get("alternatives")).get(0);
            return (String) alt.get("transcript");
        } catch (Exception e) {
            System.err.println("⚠️ Deepgram parse error: " + e.getMessage());
            return null;
        }
    }

    /**
     * 🧾 Optional: Retrieve full transcript for a meeting
     */

}

