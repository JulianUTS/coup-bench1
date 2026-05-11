package com.example.coup_bench.util;

import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Enums.Scenario;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.service.ChallengeService;

import java.util.List;

public class HumanUtil {
    public static String printGetActionPrompt(Game game, Player player){
        String actionPrompt = buildActionPrompt(game, player);
        System.out.println(actionPrompt);
        return actionPrompt;
    }

    public static String printGetReactionPrompt(Game game, ActionRecord challengedRecord, ChallengeService challengeService,
                                              Player player, Scenario scenario){
        String reactionPrompt = buildReactionPrompt(game, challengedRecord, challengeService, player, scenario);
        System.out.println(reactionPrompt);
        return reactionPrompt;
    }
    public static String printGetCardPrompt(Game game, Player player){
        String prompt = buildChooseCardPrompt(game, player);
        System.out.println(prompt);
        return prompt;
    }
    public static String printGetCardsToExchange(Game game, Player player, int cardsToExchange){
        String prompt = buildExchangeCardsPrompt(game, player, cardsToExchange);
        System.out.println(prompt);
        return prompt;
    }
    private static String buildExchangeCardsPrompt(Game game, Player player, int cardsToExchange) {
        return """
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                
                ### MEMORY
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
                  "cards": %s
                }
                
        
               
                """.formatted(
                player.getId(),
                player.getCoins(),
                String.join("\n", game.getGameMemory()),
                game.getPlayers().stream()
                        .filter(p -> !p.getId().equals(player.getId()))
                        .map(p -> p.getId() + " (" + p.getCoins() + " coins, " + p.getCards().size() + " cards)")
                        .toList(),
                cardsToExchange,
                player.getCards().stream().toList(),
                correctListFormat(cardsToExchange)
        );
    }
    private static String correctListFormat(int cardsToExchange) {
        if(cardsToExchange == 2){
            return("[string, string]");
        }
        return("[string]");
    }

    private static String buildActionPrompt(Game game, Player player) {
        return """
                Your turn to act
                
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### MEMORY
                %s
                
                Other players:
                %s
                
                Game state: Choose an action
                
                ### ALLOWED ACTIONS FOR YOU
                %s
                
                """.formatted(
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
    private static String buildReactionPrompt(Game game, ActionRecord challengedRecord, ChallengeService challengeService,
                                       Player player, Scenario scenario) {
        return """
                Your turn to react
                
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### MEMORY
                %s
                
                Other players:
                %s
                
                %s
              
                """.formatted(
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
    private static String scenarioText(ActionRecord challengedRecord, ChallengeService challengeService, Scenario scenario) {
        return switch (scenario) {
            case S1 ->

                //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged by anyone
                    """       
                            Game state: Player %s has used %s
                            
                            ### COUP RULES
                            - You may choose to challenge the current the action if you believe player %s is bluffing
                            
                            ### ALLOWED ACTIONS FOR YOU
                            | CHALLENGE | DO_NOTHING |
                            
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
                    
                            """.formatted(
                            challengedRecord.getPlayerId()
                    );
            default ->
                    """       
                            Game state: NULL scenario, return DO_NOTHING
                    
                            ### ALLOWED ACTIONS FOR YOU
                            | DO_NOTHING |
                    
                            """;
        };
    };
    private static String buildChooseCardPrompt(Game game, Player player) {
        return """     
                ### GAME INFO
                Your ID: %s
                Your coins: %d
                Your cards: %s
                
                ### MEMORY
                %s
                
                Other players:
                %s
                
                Game state: You must choose a card to loose

                """.formatted(
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
}
