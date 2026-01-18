package com.ai;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LMStudioService {
    private final WebClient webClient;

    public LMStudioService(@Value("${LM_STUDIO_URL:http://localhost:1234}") String apiUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    private String callLMStudioAPI(String prompt){
        try{
            Map<String, Object> requestBody = new HashMap<>();
            // Using a slightly more directive system prompt
            requestBody.put("model", "qwen/qwen3-8b");
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a software simulation logger. Provide a concise 3-sentence summary of the simulation run. Do not output internal thoughts."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.3); // Lower temperature for more focused answers
            requestBody.put("max_tokens", 300);
            requestBody.put("stream", false);

            Map<String, Object> response = webClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    return cleanResponse(content);
                }
            }
            return "Log entry empty.";
        } catch(Exception e){
            return "Error writing log: " + e.getMessage();
        }
    }

    // FIX: Remove <think> tags from the output
    private String cleanResponse(String raw) {
        if (raw == null) return "";
        // Regex to remove <think>...</think> content (dotall mode)
        String cleaned = raw.replaceAll("(?s)<think>.*?</think>", "").trim();
        // Remove any markdown code blocks if present
        return cleaned.replaceAll("```", "");
    }

    public String generateSimulationSummary(List<?> gameRecords){
        if (gameRecords == null || gameRecords.isEmpty()) return "No data pending analysis.";
        return callLMStudioAPI("Summarize these simulation metrics: " + gameRecords.toString());
    }
}