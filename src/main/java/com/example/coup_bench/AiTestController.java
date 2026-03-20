package com.example.coup_bench;

import com.example.coup_bench.model.Player;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/aiPrompt")
public class AiTestController {
    private final ChatClient chatClient;
    private final AiPromptBuilder aiPromptBuilder;
    private final CoupTestService coupTestService;


    public AiTestController(ChatClient chatClient, AiPromptBuilder aiPromptBuilder, CoupTestService coupTestService) {
        this.chatClient = chatClient;
        this.aiPromptBuilder = aiPromptBuilder;
        this.coupTestService = coupTestService;
    }

    @GetMapping("/test")
    public AiDecision getAiDecision() {

        // Get a test game snapshot
        var snapshot = coupTestService.playTestGame();

        // Choose the first player as the AI-controlled player
        Player player = snapshot.players().get(0);

        // Everyone else is an opponent
        List<Player> opponents = snapshot.players().subList(1, snapshot.players().size());

        // Build the AI prompt
        String prompt = aiPromptBuilder.buildExplanationPrompt(
                player,
                opponents,
                snapshot.state()
        );

        // Call the AI model
        return chatClient
                .prompt(prompt)
                .call()
                .entity(AiDecision.class);
    }
}

