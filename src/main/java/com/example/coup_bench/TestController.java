package com.example.coup_bench;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/prompt")
public class TestController {
    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;


    public TestController(ChatClient chatClient, PromptBuilder promptBuilder) {
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
    }

    @GetMapping(path = "/test")
    public TestResult generate(
            @RequestParam String topic
    ) {
        String prompt = promptBuilder.buildExplanationPrompt(topic);
        return chatClient
                .prompt(prompt)
                .call()
                .entity(TestResult.class);
    }
}

