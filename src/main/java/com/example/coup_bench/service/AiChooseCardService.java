package com.example.coup_bench.service;

import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.model.AiResponses.AiChooseCard;
import com.example.coup_bench.model.AiResponses.AiExchange;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.PromptUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class AiChooseCardService {
    private final MultiModelRouter router;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiChooseCardService(MultiModelRouter router) {
        this.router = router;
    }

    public CardType getCardToLoose(Game game, Player player) {
        int tries = 0;
        while(tries < 3) {
            String prompt = buildChooseCardPrompt(game, player);
            String response = getResponse(player.getId(), prompt);
            try {
                AiChooseCard chosenCard = mapper.readValue(response, AiChooseCard.class);
                if (player.hasCard(chosenCard.card)) {
                    return chosenCard.card;
                } else {
                    System.err.println(player.getId() + "- Invalid Cards:\n" + response);
                    game.logGameMemory(player.getId() + "has chosen invalid cards, try again");
                }
            } catch (Exception e) {
                System.err.println(player.getId() + "- Invalid JSON:\n" + response);
                game.logGameMemory(player.getId() + "has chosen invalid cards, try again");
            }
            tries++;
        }
        game.logGameMemory(player.getId() + "has chosen an invalid card 3 times, a card will be exchanged at random");
        game.logGameMemory(player.getId() + "has chosen invalid cards 3 times, cards will be exchange at random");
        return player.getCards().get(new Random().nextInt(2));
    }
    public List<CardType> getCardsToExchange(Game game, Player player, List<CardType> cardsToChooseFrom,int cardsToExchange) {
        int tries = 0;
        while(tries < 3) {
            String prompt = buildExchangeCardsPrompt(game, player, cardsToChooseFrom,cardsToExchange);
            String response = getResponse(player.getId(), prompt);
            try {
                AiExchange chosen = mapper.readValue(response, AiExchange.class);
                // Validate
                if (chosen.CardsToKeep != null && chosen.CardsToKeep.size() == cardsToExchange) {
                    boolean allValid = chosen.CardsToKeep.stream().allMatch(player::hasCard);
                    if (allValid) return chosen.CardsToKeep;
                }
                System.err.println(player.getId() + " - Invalid cards:\n" + response);
                game.logGameMemory(player.getId() + "has chosen an invalid card, try again");
            } catch (Exception e) {
                System.err.println(player.getId() + " - Invalid JSON:\n" + response);
                game.logGameMemory(player.getId() + "has chosen an invalid card, try again");
            }
            tries++;
        }
        game.logGameMemory(player.getId() + "has chosen invalid cards 3 times, cards will be exchange at random");
        return getRandomFallback(player, cardsToExchange);
    }

    private List<CardType> getRandomFallback(Player player, int count) {
        List<CardType> pool = new ArrayList<>(player.getCards());
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()));
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
                
                ### RECENT MEMORY
                %s
                
                Other players:
                %s
                
                Game state: You must choose a card to loose
                
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
                String.join("\n",
                        game.getGameMemory()
                                .stream()
                                .skip(Math.max(0, game.getGameMemory().size() - 30))
                                .toList()
                ),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList()
        );
    }
    private String buildExchangeCardsPrompt(Game game, Player player, List<CardType> cardsToChooseFrom, int cardsToExchange) {
        return """
                You are an AI agent playing Coup.
                Your playstyle:
                %s
                
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### RECENT MEMORY
                %s
                
                Other players:
                %s
                
                Game state: You have chosen to exchange your cards. Pick %s cards you would like to keep for the 
                selection below.
                
                ### CARDS SELECTION
                %s
                
                ### COUP RULES
                - These cards will not be revealed to the other players.
                -If you choose invalid cards or an incorrect amount of cards, random valid cards will be chosen instead.
                
                ### JSON SCHEMA (FOLLOW EXACTLY)
                {
                  "CardsToKeep": %s
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
                String.join("\n",
                        game.getGameMemory()
                                .stream()
                                .skip(Math.max(0, game.getGameMemory().size() - 30))
                                .toList()
                ),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList(),
                cardsToExchange,
                cardsToChooseFrom,
                correctListFormat(cardsToExchange)
        );
    }

    private String correctListFormat(int cardsToExchange) {
        if(cardsToExchange == 2){
            return("[card, card]");
        }
        return("[card]");
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
       // System.out.println(provider + ": " + response);
        return cleanResponse(response);
    }
}
