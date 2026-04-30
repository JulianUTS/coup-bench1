package com.example.coup_bench;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiAction;
import com.example.coup_bench.model.AiResponses.AiChooseCard;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;
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
        String response = getResponse(player.getId(),  prompt);

        try {
                return mapper.readValue(response, AiAction.class);

        } catch (Exception e) {
            System.err.println(player.getId() + "- Invalid JSON:\n" + response );

            AiAction fallback = new AiAction();
            fallback.action = ActionType.INVALID;
            return fallback;
        }

    }

    public AiReaction getReaction(Game game, Player player, int scenario){
        String prompt = buildReactionPrompt(game, player, scenario);
        String response = getResponse(player.getId(),  prompt);

        try {
            return mapper.readValue(response, AiReaction.class);

        } catch (Exception e) {
            System.err.println(player.getId() + "- Invalid JSON:\n" + response );

            AiReaction fallback = new AiReaction();
            fallback.action = ActionType.DO_NOTHING;
            return fallback;
        }
    }

    public CardType getCardToLoose(Game game, Player player) {
        String prompt = buildChooseCardPrompt(game, player);
        String response = getResponse(player.getId(),  prompt);

        try {
            AiChooseCard chosenCard = mapper.readValue(response, AiChooseCard.class);
            if(player.hasCard(chosenCard.card)) {
                return chosenCard.card;
            } else{
                System.err.println(player.getId() + "- Invalid Cards:\n" + response );
                CardType fallback = player.getCards().get(new Random().nextInt(player.getCards().size()));;
                return fallback;
            }

        } catch (Exception e) {
            System.err.println(player.getId() + "- Invalid JSON:\n" + response );
            CardType fallback = player.getCards().get(new Random().nextInt(player.getCards().size()));;
            return fallback;
        }

    }

    private String buildActionPrompt(Game game, Player player) {
        return """
                You are an AI agent playing Coup.
                Your playstyle:
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
                personalityPrompt(player.getPersonality()),
                player.getId(),
                player.getCoins(),
                player.getCards().stream().toList(),
                String.join("\n", game.getGameMemory()),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList(),
                game.getState(),
                allowedActions(player)
        );
    }

    private String buildChooseCardPrompt(Game game, Player player) {
        return """
                You are an AI agent playing Coup.
                Your playstyle:
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
                personalityPrompt(player.getPersonality()),
                player.getId(),
                player.getCoins(),
                player.getCards().stream().toList(),
                String.join("\n", game.getGameMemory()),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList()
        );
    }

    private String buildReactionPrompt(Game game, Player player, int scenario) {
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
                personalityPrompt(player.getPersonality()),
                player.getId(),
                player.getCoins(),
                player.getCards().stream().toList(),
                String.join("\n", game.getGameMemory()),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList(),
                scenarioText(game, scenario));
}

    private String scenarioText(Game game, int scenario) {
        return switch (scenario) {
            case 1 ->

                //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged by anyone
                    """       
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
                            game.getActingPlayerId());
            case 2 ->
                //Scenario 2- Foreign aid can be blocked anyone
                    """       
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
            case 3 ->
                //Generic setup for challenge counteraction
                //Scenario 2- Block Foreign Aid counteraction can be challenged by anyone, except the original
                //Scenario 3- Block steal counteraction can be challenged by any player, except the original
                //Scenario 4- Block assassinate counteraction can be challenged by any player, except the original

                    """       
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
            case 4 ->
                //Scenario 3- Steal can be challenged by any player, except for the target and original
                    """       
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
            case 5 ->
                //Scenario 3- Steal can be blocked by targeted player if no one wants to challenge original steal
                    """       
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
            case 6 ->
                //Scenario 4- Assassinate can be challenged by any player, except for the original
                    """       
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
            case 7 ->
                //Scenario 4- Assassinate can be blocked by targeted player if no one wants to challenge original assassinate
                    """       
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
            default ->
                    """       
                            Game state: NULL scenario, return DO_NOTHING
                    
                            ### ALLOWED ACTIONS FOR YOU
                            | DO_NOTHING |
                    
                            ### JSON SCHEMA (FOLLOW EXACTLY)
                            {
                              "action": "DO_NOTHING",
                              "reason": string
                            }
                            """;
        };
    };

    private String cleanResponse(String response) {
        return response
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .replace("\"null\"", "null");
    }

    private String getResponse(String provider, String prompt) {
        String response = router.ask(provider, prompt);
        System.out.println(response);
        return cleanResponse(response);
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

        String analyticalRules = """
        ### PERSONALITY — ANALYTICAL
        - You make decisions based on logic and probability.
        - You bluff only when the expected value is positive.
        - You challenge selectively, only when evidence is strong.
        - You prefer TAX, EXCHANGE, and safe coin‑efficient plays.
        - You avoid chaotic or impulsive actions.
        """;

        String opportunisticRules = """
        ### PERSONALITY — OPPORTUNISTIC
        - You adapt your strategy based on opponents' behavior.
        - You bluff when opponents appear passive or unlikely to challenge.
        - You challenge aggressive or suspicious opponents more often.
        - You prefer STEAL, TAX, and EXCHANGE depending on the situation.
        - You take calculated risks when they offer high reward.
        """;

        String defaultRules = """
        ### PERSONALITY — DEFAULT
        - You play with balanced, neutral strategy.
        - You bluff occasionally when it is strategically reasonable.
        - You challenge only when moderately confident.
        - You use TAX, INCOME, STEAL, and EXCHANGE without strong bias.
        - You avoid extreme risk-taking or extreme caution.
        """;

        return  (switch (personality) {
            case "aggressive" -> aggressiveRules;
            case "defensive" -> defensiveRules;
            case "chaotic" -> chaoticRules;
            case "analytical" -> analyticalRules;
            case "opportunistic" -> opportunisticRules;
            case "default" -> defaultRules;
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
}

