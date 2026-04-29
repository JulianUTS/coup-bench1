package com.example.coup_bench;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiChooseCard;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AiDecisionService {

    private final MultiModelRouter router;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiDecisionService(MultiModelRouter router) {
        this.router = router;
    }

    public AiAction getAction(Game game, Player player) {
        String prompt = buildActionPrompt(game, player);

        String response = router.ask(player.getProvider(), prompt);

       // System.out.println("[" + player.getId() + " Prompt] " + prompt);

        String cleaned = response
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .replace("\"null\"", "null");

        try {
                return mapper.readValue(cleaned, AiAction.class);

        } catch (Exception e) {
            System.err.println("Invalid AI JSON: " + response);

            AiAction fallback = new AiAction();
            fallback.action = ActionType.INVALID;
            fallback.targetId = null;
            return fallback;
        }

    }

    public CardType getCardToLoose(Game game, Player player) {
        String prompt = buildChooseCardPrompt(game, player);
      //  System.out.println("[" + player.getId() + "] " + prompt);

        String response = router.ask(player.getProvider(), prompt);
       // System.out.println("[" + player.getId() + " Prompt] " + prompt);

        String cleaned = response
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .replace("\"null\"", "null");

        try {
            AiChooseCard chosenCard = mapper.readValue(cleaned, AiChooseCard.class);
            if(player.hasCard(chosenCard.card)) {
                return chosenCard.card;
            } else{
                System.err.println("Invalid card " + response);
                CardType fallback = player.getCards().get(new Random().nextInt(player.getCards().size()));;
                return fallback;
            }

        } catch (Exception e) {
            System.err.println("Invalid AI JSON: " + response);

            CardType fallback = player.getCards().get(new Random().nextInt(player.getCards().size()));;
            return fallback;
        }

    }

    public AiReaction getReaction(Game game, Player player, int scenario){
        String prompt = buildReactionPrompt(game, player, scenario);

        String response = router.ask(player.getProvider(), prompt);

   //     System.out.println("[" + player.getId() + "] " + prompt);

        String cleaned = response
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .replace("\"null\"", "null");

        try {
            return mapper.readValue(cleaned, AiReaction.class);

        } catch (Exception e) {
            System.err.println("Invalid AI JSON: " + response);

            AiReaction fallback = new AiReaction();
            fallback.action = ActionType.DO_NOTHING;
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
        return allowedActions;
    }

    private String buildActionPrompt(Game game, Player player) {
        String personalityRules = personalityPrompt(player.getPersonality());
        String memoryText = String.join("\n", game.getGameMemory());

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
                
                Game state: %s
                
                ### COUP RULES
                - You may ONLY choose COUP if you have 7 or more coins.
                - If you have 10 or more coins, COUP is the ONLY valid action.
                - When choosing COUP, you MUST select a valid targetId (any other alive player).
                - Never choose COUP with fewer than 7 coins.
                - You can only choose ASSASSINATE if you have 3 or more coins.
                - When choosing ASSASSINATE, you can only choose a player with more than 0 coins.
                
                ### ALLOWED ACTIONS FOR YOU
                %s
                
                
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": string | null,
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
                
               
                """.formatted(
                player.getPersonality(),
                personalityRules,
                player.getId(),
                player.getCoins(),
                player.getCards().stream().toList(),
                memoryText,
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList(),
                game.getState(),
                allowedActions(player)
        );
    }

    private String buildChooseCardPrompt(Game game, Player player) {
        String personalityRules = personalityPrompt(player.getPersonality());
        String memoryText = String.join("\n", game.getGameMemory());

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
                
                Game state: You have failed a challenge and must choose a card to loose
                
                ### COUP RULES
                - This card will not be revealed to the other players, take this into
                account when choosing.
                - If you choose null or an invalid card, a random card will be chosen instead.
                
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "card": string | null,
                }
                
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
                player.getCards().stream().toList(),
                memoryText,
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList()
        );
    }

    private String buildReactionPrompt(Game game, Player player, int scenario) {
        String personalityRules = personalityPrompt(player.getPersonality());

        String memoryText = String.join("\n", game.getGameMemory());
        String scenarioText = scenarioText(game, player, scenario);

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
                player.getCards().stream().toList(),
                memoryText,
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList(),
                scenarioText);
}

    private String scenarioText(Game game, Player player, int scenario) {
        if (scenario == 1){

            //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged by anyone
            return """       
                Game state: Player %s has used %s
        
                ### COUP RULES
                - You may choose to challenge the current the action if you believe player %s is bluffing
        
                ### ALLOWED ACTIONS FOR YOU
                | CHALLENGE | DO_NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO_NOTHING" |,
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
                | BLOCK_USING_DUKE | DO_NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "BLOCK_USING_DUKE" | "DO_NOTHING" |,
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
                | CHALLENGE | DO_NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO_NOTHING" |,
                  "reason": string
                }
        
        """.formatted(
                    game.getBlockerId(),
                    game.getActingPlayerId(),
                    game.getDeclaredAction(),
                    game.getBlockingRole(),
                    game.getBlockerId()

            );
        } else if (scenario == 4){
            //Scenario 4- Steal can be challenged by any player, except for the target and original actor

            return """       
                Game state: Player %s has used STEAL on player %s
        
                ### COUP RULES
                - You may choose to challenge the STEAL if you believe player %s is bluffing
        
                ### ALLOWED ACTIONS FOR YOU
                | CHALLENGE | DO_NOTHING |
 
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": "CHALLENGE" | "DO_NOTHING" |,
                  "reason": string
                }
        
        """.formatted(
                    game.getActingPlayerId(),
                    game.getTargetId(),
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
                  "action": "CHALLENGE" | "DO_NOTHING" |,
                  "reason": string
                }
        
        """.formatted(
                    game.getActingPlayerId(),
                    game.getTargetId(),
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
                  "action": "BLOCK_USING_CONTESSA" | "DO_NOTHING" |,
                  "reason": string
                }
        
        """.formatted(
                    game.getActingPlayerId()

            );
        }
        return """
                Game state: %s
                
                ### COUP RULES
                - You may ONLY choose COUP if you have 7 or more coins.
                - If you have 10 or more coins, COUP is the ONLY valid action.
                - When choosing COUP, you MUST select a valid targetId (any other alive player).
                - Never choose COUP with fewer than 7 coins.
                - You can only choose ASSASSINATE if you have 3 or more coins.
                - When choosing ASSASSINATE, you can only choose a player with more than 0 coins.
                
                ### ALLOWED ACTIONS FOR YOU
                %s
                
                
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "action": string | null,
                  "targetId": string | null,
                  "reason": string
                }
                """.formatted(
                game.getState(),
                allowedActions(player));
    };
}

