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
public class DeepSeekClient {

    private final RestClient client;
    private final String model = "deepseek-chat"; // or deepseek-reasoner

    public DeepSeekClient() {

        HttpClient jdk = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(Duration.ofSeconds(60));

        this.client = RestClient.builder()
                .baseUrl("https://api.deepseek.com")
                .requestFactory(factory)
                .build();
    }

    @SuppressWarnings("unchecked")
    public String chat(String prompt) {

        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        Map<String, Object> response = client.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + System.getenv("DEEPSEEK_API_KEY"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}

