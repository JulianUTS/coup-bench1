package com.example.coup_bench;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.ActionType;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AiDecisionService {

    private final MultiModelRouter router;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiDecisionService(MultiModelRouter router) {
        this.router = router;
    }

    public AiDecision decide(Game game, Player player) {

        String prompt = buildPrompt(game, player);
        String response = router.ask(player.getProvider(), prompt);
        String cleaned = response
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .replace("\"null\"", "null");

        try {
            AiDecision decision = mapper.readValue(cleaned, AiDecision.class);

            // 🔥 CRITICAL: Guarantee action is never null
            if (decision.action == null) {
                decision.action = ActionType.INCOME;
            }

            return decision;

        } catch (Exception e) {
            System.err.println("Invalid AI JSON: " + response);
            e.printStackTrace();

            AiDecision fallback = new AiDecision();
            fallback.action = ActionType.INCOME;
            fallback.targetId = null;
            fallback.block = false;
            fallback.challenge = false;
            return fallback;
        }

    }


    private String buildPrompt(Game game, Player player) {

        return """
        You are an AI agent playing Coup.

        Your ID: %s
        Your coins: %d
        Your cards: %s
        Alive: %s

        Other players:
        %s

        Game state: %s
        Declared action: %s
        Acting player: %s
        Blocking player: %s
        Challenger: %s

        Respond ONLY in JSON:
        {
          "action": "INCOME | FOREIGN_AID | TAX | STEAL | ASSASSINATE | COUP | EXCHANGE | null",
          "targetId": "player-id-or-null",
          "block": true/false,
          "challenge": true/false
        }
        
        Respond with ONLY valid JSON.
        Do NOT include backticks.
        Do NOT include markdown.
        Do NOT include explanations.
        Do NOT include code blocks.
        If you cannot decide, output exactly:
        {"action":"INCOME","targetId":null,"block":false,"challenge":false}
        
        Never output "null" as a string.
        If you do not want to take an action, output:
        "action": null
     
        """.formatted(
                player.getId(),
                player.getCoins(),
                player.getCards().stream().map(c -> c.getType().name()).toList(),
                player.isAlive(),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins)")
                        .toList(),
                game.getState(),
                game.getDeclaredAction(),
                game.getActingPlayerId(),
                game.getBlockingPlayerId(),
                game.getChallengerId()
        );
    }
}

