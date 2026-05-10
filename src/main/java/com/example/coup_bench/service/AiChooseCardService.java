package com.example.coup_bench.service;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.AiResponses.AiChooseCard;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.PromptUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AiChooseCardService {
    private final MultiModelRouter router;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiChooseCardService(MultiModelRouter router) {
        this.router = router;
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
                CardType fallback = player.getCards().get(new Random().nextInt(2));;
                return fallback;
            }

        } catch (Exception e) {
            System.err.println(player.getId() + "- Invalid JSON:\n" + response );
            CardType fallback = player.getCards().get(new Random().nextInt(2));
            return fallback;
        }

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
                PromptUtil.getPersonalityPrompt(player.getPersonality()),
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

    private String cleanResponse(String response) {
        return response
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .replace("\"null\"", "null");
    }

    private String getResponse(String provider, String prompt) {
        // System.out.println(prompt);
        String response = router.ask(provider, prompt);
     //   System.out.println(provider + ": " + response);
        return cleanResponse(response);
    }
}
