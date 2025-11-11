package com.example.interviewsitebackend.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class WhisperService {

    private static final String HF_URL = "https://api-inference.huggingface.co/models/openai/whisper-small";
    private static final String HF_TOKEN = "YOUR_HF_API_KEY"; // replace with your token

    public String transcribe(byte[] audioBytes) {
        try {
            URL url = new URL(HF_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(audioBytes);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            if (root.has("text")) return root.get("text").asText("");
            if (root.isArray() && root.size() > 0) return root.get(0).path("text").asText("");
            return "";

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

