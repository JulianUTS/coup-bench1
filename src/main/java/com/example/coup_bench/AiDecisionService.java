package com.example.coup_bench;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiDecisionService {

    private final MultiModelRouter router;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiDecisionService(MultiModelRouter router) {
        this.router = router;
    }

    public AiDecision decide(Game game, Player player, int scenario) {
        String prompt;

        if (scenario == 1){
            prompt = buildScenario1(game, player);
        } else if (scenario == 2){
            prompt = buildScenario2(game, player);
        } else {
            prompt = buildScenario0(game, player);
        }

        String response = router.ask(player.getProvider(), prompt);

        System.out.println("[" + player.getId() + "] " + response);

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
            return fallback;
        }

    }

    private String personalityPrompt( String personality ) {
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

        return  (switch (personality) {
            case "aggressive" -> aggressiveRules;
            case "defensive" -> defensiveRules;
            case "chaotic" -> chaoticRules;
            default -> "";
        });
    }

    private List<String> allowedActions( Player player){
        List<String> allowedActions = new ArrayList<>();
        allowedActions.add("INCOME");
        allowedActions.add("FOREIGN_AID");
        allowedActions.add("TAX");
        allowedActions.add("ASSASSINATE");
        allowedActions.add("EXCHANGE");
        allowedActions.add("STEAL");

        if (player.getCoins() >= 7) {
            allowedActions.add("COUP");
        }

        if (player.getCoins() >= 10) {
            allowedActions.clear();
            allowedActions.add("COUP");
        }
        System.out.println(allowedActions);
        return allowedActions;
    }

    private List<String> allowedReactions( Player player, Action reaction){
        List<String> allowedActions = new ArrayList<>();
        allowedActions.add("INCOME");
        allowedActions.add("FOREIGN_AID");
        if (player.hasCard(CardType.DUKE)){
            allowedActions.add("TAX");
        } else {
            allowedActions.add("BLUFF TAX");
        }
        if (player.hasCard(CardType.ASSASSIN)){
            allowedActions.add("ASSASSINATE");
        } else {
            allowedActions.add("BLUFF ASSASSINATE");
        }
        if (player.hasCard(CardType.AMBASSADOR)){
            allowedActions.add("EXCHANGE");
        } else {
            allowedActions.add("BLUFF EXCHANGE");
        }
        if (player.hasCard(CardType.CAPTAIN)){
            allowedActions.add("STEAL");
        } else {
            allowedActions.add("BLUFF STEAL");
        }

        if (player.getCoins() >= 7) {
            allowedActions.add("COUP");
        }

        if (player.getCoins() >= 10) {
            allowedActions.clear();
            allowedActions.add("COUP");
        }
        System.out.println(allowedActions);
        return allowedActions;
    }

    private String buildScenario0(Game game, Player player) {

        String personalityRules = personalityPrompt(player.getPersonality());

        List<String> allowedActions = allowedActions(player);
        String memoryText = String.join("\n", player.getMemory().history);


        System.out.println(memoryText);


        return """
                You are an AI agent playing Coup.
                Your playstyle: %s
                
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### MEMORY
                %s
                
                Other players:
                %s
                
                Game state: %s
                Declared action: %s
                Acting player: %s
                Blocking player: %s
                Challenger: %s
                
                ### COUP RULES
                - You can choose any action even if you do not have the valid card.
                - You may ONLY choose COUP if you have 7 or more coins.
                - If you have 10 or more coins, COUP is the ONLY valid action.
                - When choosing COUP, you MUST select a valid targetId (any other alive player).
                - Never choose COUP with fewer than 7 coins.
                
                ### ALLOWED ACTIONS FOR YOU
                %s
                
                ### ACTION RULES
                - If it is YOUR turn:
                    - If you have 10 or more coins, you MUST choose:
                        "action": "COUP"
                        "targetId": the ID of any other alive player
                        "block": false
                        "challenge": false
                    - Otherwise choose exactly ONE allowed action.
                    - Set block = false and challenge = false.
                
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "INCOME" | "FOREIGN_AID" | "TAX" | "STEAL" | "ASSASSINATE" | "COUP" | "EXCHANGE",
                  "targetId": string | null,
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
                {"action":"INCOME","targetId":null,"reason":"fallback"}
                
                %s
                """.formatted(
                player.getPersonality(),
                player.getId(),
                player.getCoins(),
                player.getCards().stream().map(c -> c.getType().name()).toList(),
                memoryText,
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins)")
                        .toList(),
                game.getState(),
                game.getDeclaredAction(),
                game.getActingPlayerId(),
                game.getBlockingPlayerId(),
                game.getChallengerId(),
                allowedActions,          // <-- FIXED: now included
                personalityRules         // <-- FIXED: still included
        );

    }

    private String buildScenario1(Game game, Player player) {
        String personalityRules = personalityPrompt(player.getPersonality());


        String memoryText = String.join("\n", player.getMemory().history);


        System.out.println(memoryText);


        return """
                You are an AI agent playing Coup.
                %s
                
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### MEMORY
                %s
                
                Other players:
                %s
                
                Game state: Player %s has used %s
                
                ### COUP RULES
                - You may choose to challenge the current %s action if you believe %s is bluffing
                
                ### ALLOWED ACTIONS FOR YOU
                | CHALLENGE | DO NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO NOTHING" |,
                  "targetId": string | null,
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
                
                
                """.formatted(
                personalityRules,
                player.getId(),
                player.getCoins(),
                player.getCards().stream().map(c -> c.getType().name()).toList(),
                memoryText,
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins)")
                        .toList(),
                game.getActingPlayerId(),
                game.getDeclaredAction(),
                game.getDeclaredAction(),
                game.getActingPlayerId()

        );
    }
    private String buildScenario2(Game game, Player player) {
        String personalityRules = personalityPrompt(player.getPersonality());


        String memoryText = String.join("\n", player.getMemory().history);


        System.out.println(memoryText);


        return """
                You are an AI agent playing Coup.
                %s
                
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### MEMORY
                %s
                
                Other players:
                %s
                
                Game state: Player %s has used %s
                
                
                ### COUP RULES
                - You may choose to block the foreign aid if you have a duke, or want to bluff having a duke
                
                ### ALLOWED ACTIONS FOR YOU
                | BLOCK | DO NOTHING |
                
                
                
                - If it is NOT your turn (someone else declared an action):
                    - Ignore the "action" field.
                    - Always set:
                        "action": "INCOME"
                        "targetId": null
                    - Decide ONLY whether to block or challenge.
                    - If the declared action is INCOME, you MUST set:
                        "block": false
                        "challenge": false
                
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "BLOCK" | "DO NOTHING" |,
                  "targetId": string | null,
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
                
                
                """.formatted(
                personalityRules,
                player.getId(),
                player.getCoins(),
                player.getCards().stream().map(c -> c.getType().name()).toList(),
                memoryText,
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins)")
                        .toList(),
                game.getActingPlayerId(),
                game.getDeclaredAction()

        );
    }

    private String buildScenario3(Game game, Player player) {
        String personalityRules = personalityPrompt(player.getPersonality());


        String memoryText = String.join("\n", player.getMemory().history);


        System.out.println(memoryText);


        return """
                You are an AI agent playing Coup.
                %s
                
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### MEMORY
                %s
                
                Other players:
                %s
                
                Game state: Player %s has blocked your action, would you like to challenge
                
                
                ### COUP RULES
                - You may choose to challenge the block if you believe the player is bluffing
                
                ### ALLOWED ACTIONS FOR YOU
                | CHALLENGE | DO NOTHING |
                
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO NOTHING" |,
                  "targetId": string | null,
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
                
                
                """.formatted(
                personalityRules,
                player.getId(),
                player.getCoins(),
                player.getCards().stream().map(c -> c.getType().name()).toList(),
                memoryText,
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins)")
                        .toList(),
                game.getActingPlayerId()


        );
    }
}

