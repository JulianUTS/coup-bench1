package com.example.coup_bench;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptBuilder {

    public String buildExplanationPrompt(String topic) {
        PromptTemplate template = new PromptTemplate("""
    Write a haiku about the topic: {topic}.

    Return ONLY a JSON object with these fields:
    - line1
    - line2
    - line3
    - theme
    """);

        return template.create(Map.of(
                "topic", topic
        )).getContents();
    }
}

