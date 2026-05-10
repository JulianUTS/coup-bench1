package com.example.coup_bench.service;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiAction;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.util.PromptUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AiActionService {

    private final MultiModelRouter router;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiActionService(MultiModelRouter router) {
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
                
                Game state: Choose an action
                
                ### COUP RULES
                - You may ONLY choose COUP if you have 7 or more coins.
                - If you have 10 or more coins, COUP is the ONLY valid action.
                - When choosing COUP, you MUST select a valid targetId (any other alive player).
                - Never choose COUP with fewer than 7 coins.
                - You can only choose ASSASSINATE if you have 3 or more coins.
                - When choosing ASSASSINATE, you can only choose a player with more than 0 coins.
                - When choosing EXCHANGE, all your remaining cards will be automatically switched out
                with random cards, you cannot choose which cards to EXCHANGE
                
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
                PromptUtil.getPersonalityPrompt(player.getPersonality()),
                player.getId(),
                player.getCoins(),
                player.getCards().stream().toList(),
                String.join("\n", game.getGameMemory()),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList(),
                PromptUtil.allowedActions(player)
        );
    }

    private String getResponse(String provider, String prompt) {
       // System.out.println(prompt);
        String response = router.ask(provider, prompt);
        System.out.println(provider + ": " + response);
        return PromptUtil.cleanResponse(response);
    }


}

