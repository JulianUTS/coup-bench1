package com.example.coup_bench.service;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.Scenario;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.PromptUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AiReactionService {
    private final MultiModelRouter router;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiReactionService(MultiModelRouter router) {
        this.router = router;
    }

    public AiReaction getReaction(Game game, ActionRecord challengedRecord, ChallengeService challengeService,
                                  Player player, Scenario scenario){
        String prompt = buildReactionPrompt(game, challengedRecord, challengeService, player, scenario);
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

    private String buildReactionPrompt(Game game, ActionRecord challengedRecord, ChallengeService challengeService,
                                       Player player, Scenario scenario) {
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
                - "reason" MUST be a short explanation (max 30 words).
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
                PromptUtil.getPersonalityPrompt(player.getPersonality()),
                player.getId(),
                player.getCoins(),
                player.getCards().stream().toList(),
                String.join("\n", game.getGameMemory()),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList(),
                scenarioText(challengedRecord, challengeService, scenario));
    }
    private String scenarioText(ActionRecord challengedRecord, ChallengeService challengeService, Scenario scenario) {
        return switch (scenario) {
            case S1 ->

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
                            challengedRecord.getPlayerId(),
                            challengedRecord.getAction(),
                            challengedRecord.getPlayerId());
            case S2_1 ->
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
                            challengedRecord.getPlayerId()
                    );
            case S2_2,S3_3,S4_3 ->
                //Generic setup for challenge counteraction
                //Scenario 2- Block Foreign Aid counteraction can be challenged by anyone, except the original
                //Scenario 3- Block steal counteraction can be challenged by any player, except the original
                //Scenario 4- Block assassinate counteraction can be challenged by any player, except the original

                    """       
                            Game state: Player %s has claimed %s on player %s's %s
                            
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
                            challengeService.getBlockerId(),
                            challengeService.getBlockAction(),
                            challengedRecord.getPlayerId(),
                            challengedRecord.getAction(),
                            challengeService.getBlockerId()
                    );
            case S3_1 ->
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
                            challengedRecord.getPlayerId(),
                            challengedRecord.getTargetId(),
                            challengedRecord.getPlayerId()
                    );
            case S3_2 ->
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
                            challengedRecord.getPlayerId()
                    );
            case S4_1 ->
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
                            challengedRecord.getPlayerId(),
                            challengedRecord.getTargetId(),
                            challengedRecord.getPlayerId()

                    );
            case S4_2 ->
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
                            challengedRecord.getPlayerId()
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


    private String getResponse(String provider, String prompt) {
        // System.out.println(prompt);
        String response = router.ask(provider, prompt);
      //  System.out.println(provider + ": " + response);
        return PromptUtil.cleanResponse(response);
    }

}
