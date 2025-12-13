package com.ai;

import com.gameoflife.GameRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LMStudioService {
    private final WebClient webClient;
    private static final String LMSTUDIO_API_URL = "http://localhost:1234";
    public LMStudioService(){
        this.webClient = WebClient.builder()
                .baseUrl(LMSTUDIO_API_URL)
                .build();
    }

    private String callLMStudioAPI(String prompt){
        try{
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen/qwen3-v1-8b");
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a biology and simulation analyst who provides clear, insightful summaries."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_new_tokens", 600); // max response (~600 words)
            requestBody.put("stream", false);

            Map<String, Object> response = webClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "No response from LM Studio";
        } catch(WebClientException e){
            return "Error calling LM Studio API: " + e.getMessage();
        } catch(Exception e){
            return "Error: " + e.getMessage() + ". Make sure LM Studio server is running on port 1234.";
        }
    }


    // Prompt instructions for the summary

    public String generateSimulationSummary(List<?> gameRecords){
        if (gameRecords == null || gameRecords.isEmpty()){
            return "No simulation data available for summary.";
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze these Game of Life simulation runs and provide a comprehensive summary:\n\n");

        for (int i = 0; i < gameRecords.size(); i++) {
            GameRecord game = (GameRecord) gameRecords.get(i);
            prompt.append(String.format("Game %d:\n", i + 1));
            prompt.append(String.format("  - Duration: %d seconds\n", game.getDurationSeconds()));
            prompt.append(String.format("  - Total cells created: %d\n", game.getTotalCells()));
            prompt.append(String.format("  - Total sexual cells: %d\n", game.getTotalSexualCells()));
            prompt.append(String.format("  - Total asexual cells: %d\n", game.getTotalAsexualCells()));
            prompt.append(String.format("  - Alive sexual cells (at end): %d\n", game.getAliveSexualCells()));
            prompt.append(String.format("  - Alive asexual cells (at end): %d\n", game.getAliveAsexualCells()));
            prompt.append(String.format("  - Number of divisions: %d\n", game.getNrDivisions()));
            prompt.append(String.format("  - Number of reproductions: %d\n\n", game.getNrReproductions()));
        }

        prompt.append("Based on these games:\n");
        prompt.append("1. Identify patterns in cell survival rates (which type survived better?)\n");
        prompt.append("2. Compare asexual division vs sexual reproduction effectiveness\n");
        prompt.append("3. Analyze the relationship between game duration and cell population dynamics\n");
        prompt.append("4. Determine if there's a pattern in which reproduction strategy was more successful\n");

        return callLMStudioAPI(prompt.toString());

    }


    public String testConnection() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("/v1/models")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return "Connection successful! Models: " + response.toString();
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }
}
