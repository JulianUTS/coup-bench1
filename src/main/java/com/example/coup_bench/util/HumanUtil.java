package com.example.coup_bench.util;

import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;

public class HumanUtil {
    public static void printGetActionPrompt(Game game, Player player){
        String actionPrompt = buildActionPrompt(game, player);
        System.out.println(actionPrompt);
    }

    private static String buildActionPrompt(Game game, Player player) {
        return """
                You are playing Coup.
                
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
}
