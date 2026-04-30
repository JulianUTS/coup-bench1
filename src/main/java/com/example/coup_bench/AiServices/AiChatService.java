package com.example.coup_bench.AiServices;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatService {

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel model) {
        return ChatClient.create(model);
    }

    @Bean
    public ChatClient claudeChatClient(AnthropicChatModel model) {
        return ChatClient.create(model);
    }

}




