package com.example.coup_bench.AiServices;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;



@Service
public class MultiModelRouter {

    public enum Provider {
        OPENAI,
        CLAUDE,
        GEMINI,
        GROK
    }

    private final ChatClient openai;
    private final ChatClient claude;
    private final ChatClient gemini;
    private final GrokClient grok;

    public MultiModelRouter(
            ChatClient openAiChatClient,
            ChatClient claudeChatClient,
            ChatClient geminiChatClient,
            GrokClient grokClient
    ) {
        this.openai = openAiChatClient;
        this.claude = claudeChatClient;
        this.gemini = geminiChatClient;
        this.grok = grokClient;
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
            case "grok":
                p = Provider.GROK;
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
        return switch (provider) {
            case OPENAI -> openai.prompt().user(prompt).call().content();
            case CLAUDE -> claude.prompt().user(prompt).call().content();
            case GEMINI -> gemini.prompt().user(prompt).call().content();
            case GROK -> grok.chat(prompt);
        };
    }
}
