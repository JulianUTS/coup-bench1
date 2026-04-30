package com.example.coup_bench.AiServices;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GrokClient {

    private final RestClient client;
    private final String model = "grok-3-mini";

    public GrokClient() {
        HttpClient jdkHttpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdkHttpClient);
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.client = RestClient.builder()
                .baseUrl("https://api.x.ai/v1")
                .defaultHeader("Authorization", "Bearer " + System.getenv("GROK_API_KEY"))
                .requestFactory(factory)
                .build();
    }

    public String chat(String prompt) {
        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        Map<String, Object> response = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}

