package com.example.coup_bench;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/prompt")
public class TestController {
    private final PromptBuilder promptBuilder;


    public TestController(PromptBuilder promptBuilder) {
        this.promptBuilder = promptBuilder;
    }

    @GetMapping(path = "/test")
    public String generate(
            @RequestParam String topic,
            @RequestParam(defaultValue = "3") int depth
    ) {
        return promptBuilder.buildExplanationPrompt(topic, depth);
    }
}

