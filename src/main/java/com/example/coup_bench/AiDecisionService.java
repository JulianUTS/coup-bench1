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
        String prompt = buildPrompt(game, player, scenario);

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
        allowedActions.add("TAX");
        allowedActions.add("EXCHANGE");

        allowedActions.add("STEAL");

        if (player.getCoins() >= 3) {
            allowedActions.add("ASSASSINATE");
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

    private String buildPrompt(Game game, Player player, int scenario) {
        String personalityRules = personalityPrompt(player.getPersonality());

        List<String> allowedActions = allowedActions(player);
        String memoryText = String.join("\n", player.getMemory().history);
        String scenarioText = scenarioText(game, player, scenario);

        System.out.println(memoryText);

        return """
                You are an AI agent playing Coup.
                Your playstyle: %s
                %s
                
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### MEMORY
                %s
                
                Other players:
                %s
                
                %s
                
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
                
               
                """.formatted(
                player.getPersonality(),
                personalityRules,
                player.getId(),
                player.getCoins(),
                player.getCards().stream().map(c -> c.getType().name()).toList(),
                memoryText,
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins)")
                        .toList(),
                scenarioText
        );
}

    private String scenarioText(Game game, Player player, int scenario) {
        if (scenario == 1){

            //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged by anyone
            return """       
                Game state: Player %s has used %s
        
                ### COUP RULES
                - You may choose to challenge the current the action if you believe player %s is bluffing
        
                ### ALLOWED ACTIONS FOR YOU
                | CHALLENGE | DO NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO NOTHING" |,
                  "targetId": string | null,
                  "reason": string
                }
        
        """.formatted(
                game.getActingPlayerId(),
                    game.getDeclaredAction(),
                    game.getActingPlayerId()
            );

        } else if (scenario == 2){
            //Scenario 2- Foreign aid can be blocked anyone, except the original actor
            return """       
                Game state: Player %s has used FOREIGN_AID
        
                ### COUP RULES
                - You may choose to block FOREIGN_AID by claiming DUKE
                - Players may choose to challenge your claim if you are bluffing so
                choose carefully
        
                ### ALLOWED ACTIONS FOR YOU
                | BLOCK_USING_DUKE | DO NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "BLOCK_USING_DUKE" | "DO NOTHING" |,
                  "targetId": string | null,
                  "reason": string
                }
        
        """.formatted(
                    game.getActingPlayerId()
            );
        } else if (scenario == 3){
            //Scenario 3- Blocked Foreign Aid counteraction can be challenged by anyone, except the original actor and blocker
            //Scenario 6- Block steal counteraction can be challenged by any player, except the orginal
            //Scenario 9- Block assassinate counteraction can be challenged by any player, except the original

            //Generic setup for challenge counteraction
            //Scenario 3, 6, 9

            return """       
                Game state: Player %s has BLOCKED player %s's %s claiming %s
        
                ### COUP RULES
                - You may choose to challenge the block if you believe player %s is bluffing
        
                ### ALLOWED ACTIONS FOR YOU
                | CHALLENGE | DO NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO NOTHING" |,
                  "targetId": string | null,
                  "reason": string
                }
        
        """.formatted(
                    game.getBlockingPlayerId(),
                    game.getActingPlayerId(),
                    game.getDeclaredAction(),
                    game.getBlockingRole(),
                    game.getBlockingPlayerId()

            );
        } else if (scenario == 4){
            //Scenario 4- Steal can be challenged by any player, except for the target and original actor

            return """       
                Game state: Player %s has used STEAL on player %s
        
                ### COUP RULES
                - You may choose to challenge the STEAL if you believe player %s is bluffing
        
                ### ALLOWED ACTIONS FOR YOU
                | CHALLENGE | DO NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO NOTHING" |,
                  "targetId": string | null,
                  "reason": string
                }
        
        """.formatted(
                    game.getActingPlayerId(),
                    game.getTargetPlayerId(),
                    game.getActingPlayerId()
            );
        } else if (scenario == 5){
            //Scenario 5- Steal can be blocked by targeted player if no one wants to challenge original steal
            return """       
                Game state: Player %s has used their CAPTAIN to STEAL from you
        
                ### COUP RULES
                - You may choose to block the steal by claiming CAPTAIN or AMBASSADOR
                - Players may choose to challenge your claim if you are bluffing so
                choose carefully
        
                ### ALLOWED ACTIONS FOR YOU
                | BLOCK_USING_CAPTAIN | BLOCK_USING_AMBASSADOR | DO_NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "BLOCK_USING_CAPTAIN" | "BLOCK_USING_AMBASSADOR" | "DO_NOTHING" |,
                  "targetId": string | null,
                  "reason": string
                }
        
        """.formatted(
                    game.getActingPlayerId()
            );
        } else if (scenario == 6){
            //Scenario 7- Assassinate can be challenged by any player, except for the original actor
            return """       
                Game state: Player %s has used ASSASSINATE on player %s
        
                ### COUP RULES
                -You may choose to challenge the steal if you believe player %s is bluffing
                -If you are the target of the ASSASSINATE, you have to chance to block if no one else
                uses CHALLENGE, so decide carefully
        
                ### ALLOWED ACTIONS FOR YOU
                | CHALLENGE | DO_NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO NOTHING" |,
                  "targetId": string | null,
                  "reason": string
                }
        
        """.formatted(
                    game.getActingPlayerId(),
                    game.getTargetPlayerId(),
                    game.getActingPlayerId()

            );
        } else if (scenario == 7) {
            //Scenario 8- Assassinate can be blocked by targeted player if no one wants to challenge original assassinate
            return """       
                Game state: Player %s has used their ASSASSIN to ASSASSINATE you
        
                ### COUP RULES
                -You may choose to block the ASSASSINATE by claiming CONTESSA
                - Players may choose to challenge your claim if you are bluffing so
                choose carefully
        
                ### ALLOWED ACTIONS FOR YOU
                | BLOCK_USING_CONTESSA | DO_NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "BLOCK_USING_CONTESSA" | "DO NOTHING" |,
                  "targetId": string | null,
                  "reason": string
                }
        
        """.formatted(
                    game.getActingPlayerId()

            );
        }
        return """
                Game state: %s
                
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
                """.formatted(
                game.getState(),
                allowedActions(player));
    };
}

