package com.example.coup_bench;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prompt")
public class AiTestController {
    private final ChatClient chatClient;
    private final AiPromptBuilder aiPromptBuilder;


    public AiTestController(ChatClient chatClient, AiPromptBuilder aiPromptBuilder) {
        this.chatClient = chatClient;
        this.aiPromptBuilder = aiPromptBuilder;
    }

    @GetMapping(path = "/test")
    public AiTestResult generate(
            @RequestParam String topic
    ) {
        String prompt = aiPromptBuilder.buildExplanationPrompt(topic);
        return chatClient
                .prompt(prompt)
                .call()
                .entity(AiTestResult.class);
    }
}

