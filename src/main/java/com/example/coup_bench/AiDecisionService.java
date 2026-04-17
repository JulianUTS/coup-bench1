package com.example.coup_bench;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.ActionType;
import com.example.coup_bench.model.AiDecision;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
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
        System.out.println(prompt);
        System.out.println("[RAW AI OUTPUT] " + response);
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
        String aggressiveRules = """
                ### PERSONALITY — AGGRESSIVE
                - You prefer high‑impact actions.
                - You prioritize COUP, ASSASSINATE, STEAL, and TAX.
                - You rarely choose INCOME unless forced.
                - You frequently challenge opponents.
                - You block aggressively whenever possible.
                """;

        String defensiveRules = """
                ### PERSONALITY — DEFENSIVE
                - You avoid unnecessary risks.
                - You rarely challenge unless confident.
                - You block only when safe.
                - You prefer TAX, INCOME, and EXCHANGE.
                - You avoid STEAL unless advantageous.
                """;

        String chaoticRules = """
                ### PERSONALITY — CHAOTIC
                - You choose actions unpredictably.
                - You challenge frequently.
                - You block aggressively even when risky.
                - You may choose EXCHANGE or STEAL unexpectedly.
                """;

        String personalityRules = switch (player.getPersonality()) {
            case "aggressive" -> aggressiveRules;
            case "defensive" -> defensiveRules;
            case "chaotic" -> chaoticRules;
            default -> "";
        };

        return """
                You are an AI agent playing Coup.
                Your playstyle: %s
                
                ### GAME INFO
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
                
                ### ACTION RULES
                - If it is YOUR turn:
                    - If you have 10 or more coins, you MUST choose:
                        "action": "COUP"
                        "targetId": the ID of any other alive player
                        "block": false
                        "challenge": false
                    - Otherwise choose exactly ONE of the 7 valid actions.
                    - Set block = false and challenge = false.
                
                - If it is NOT your turn (someone else declared an action):
                    - Ignore the "action" field.
                    - Always set:
                        "action": "INCOME"
                        "targetId": null
                    - Decide ONLY whether to block or challenge.
                    - If the declared action is INCOME, you MUST set:
                        "block": false
                        "challenge": false
                
                ### VALID ACTIONS
                INCOME, FOREIGN_AID, TAX, STEAL, ASSASSINATE, COUP, EXCHANGE
                
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "INCOME" | "FOREIGN_AID" | "TAX" | "STEAL" | "ASSASSINATE" | "COUP" | "EXCHANGE",
                  "targetId": string | null,
                  "block": boolean,
                  "challenge": boolean,
                  "reason": string
                }
                
                ### EXPLANATION RULES
                - "reason" MUST be a short explanation (max 12 words).
                - No markdown, no quotes, no special characters.
                - Explanation MUST reflect your personality.
                
                ### OUTPUT RULES
                - Respond with ONLY the JSON object.
                - No markdown, no backticks, no text outside JSON.
                - Never output the string "null". Use actual null for targetId only.
                - All fields MUST be present.
                - Never output missing or extra fields.
                - Never output lowercase action names.
                
                ### FALLBACK
                If you cannot decide, output:
                {"action":"INCOME","targetId":null,"block":false,"challenge":false,"reason":"fallback"}
                
                %s
                """.formatted(
                player.getPersonality(),
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
                game.getChallengerId(),
                personalityRules
        );
    }
}

