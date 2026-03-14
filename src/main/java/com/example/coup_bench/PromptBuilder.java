package com.example.coup_bench;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptBuilder {

    public String buildExplanationPrompt(String topic, int depth) {
        PromptTemplate template = new PromptTemplate("""
                Write a short haiku about the topic: {topic}.
                Follow the traditional 5-7-5 syllable structure.
                Keep the tone poetic and concise.
                """);

        return template.create(Map.of(
                "topic", topic
        )).getContents();
    }
}

