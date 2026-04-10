package com.example.coup_bench;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;

@Configuration
public class AiChatService {

    // --- OpenAI ---
    @Bean
    public ChatClient openAiClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    // --- Claude (Anthropic) ---
    @Bean
    public ChatClient claudeClient(AnthropicChatModel anthropicChatModel) {
        return ChatClient.builder(anthropicChatModel).build();
    }

    // --- Gemini ---
    @Bean
    public ChatClient geminiClient(VertexAiGeminiChatModel geminiModel) {
        return ChatClient.builder(geminiModel).build();
    }
}




