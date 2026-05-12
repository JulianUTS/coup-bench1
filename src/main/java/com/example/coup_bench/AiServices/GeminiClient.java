package com.example.coup_bench.AiServices;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final RestClient client;
    private final String model = "gemini-3.1-flash-lite"; // choose your model

    public GeminiClient() {

        HttpClient jdk = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(Duration.ofSeconds(60));

        this.client = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(factory)
                .build();
    }

    @SuppressWarnings("unchecked")
    public String chat(String prompt) {

        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        Map<String, Object> response = client.post()
                .uri("/models/" + model + ":generateContent?key=" + System.getenv("GEMINI_API_KEY"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        return (String) parts.get(0).get("text");
    }
}

