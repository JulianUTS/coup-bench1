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

        var snapshot = coupTestService.playTestGame();

        Player player = snapshot.players().get(0);

        List<Player> opponents = snapshot.players().subList(1, snapshot.players().size());

        String prompt = aiPromptBuilder.buildExplanationPrompt(
                player,
                opponents,
                snapshot.state()
        );

        return chatClient
                .prompt(prompt)
                .call()
                .entity(AiDecision.class);
    }
}

