package com.example.coup_bench.AiServices;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;



@Service
public class MultiModelRouter {

    public enum Provider {
        OPENAI,
        CLAUDE,
        GEMINI
    }

    private final ChatClient openai;
    private final ChatClient claude;
    private final ChatClient gemini;

    public MultiModelRouter(
            ChatClient openAiChatClient,
            ChatClient claudeChatClient,
            ChatClient geminiChatClient
    ) {
        this.openai = openAiChatClient;
        this.claude = claudeChatClient;
        this.gemini = geminiChatClient;
    }

    // -------------------------------
    // 1. String-based entry point
    // -------------------------------
    public String ask(String provider, String prompt) {
        Provider p;

        switch (provider.toLowerCase()) {
            case "openai":
                p = Provider.OPENAI;
                break;
            case "claude":
                p = Provider.CLAUDE;
                break;
            case "gemini":
                p = Provider.GEMINI;
                break;
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        return ask(p, prompt);
    }

    // -------------------------------
    // 2. Enum-based internal method
    // -------------------------------
    public String ask(Provider provider, String prompt) {
        ChatClient client = switch (provider) {
            case OPENAI -> openai;
            case CLAUDE -> claude;
            case GEMINI -> gemini;
        };

        return client
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
