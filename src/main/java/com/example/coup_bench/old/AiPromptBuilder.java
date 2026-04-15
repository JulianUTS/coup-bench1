package com.example.coup_bench.old;

import com.example.coup_bench.model.GameState;
import com.example.coup_bench.model.Player;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiPromptBuilder {

    public String buildExplanationPrompt(Player player, List<Player> opponents, GameState state) {
        PromptTemplate template = new PromptTemplate("""
                You are an AI Coup strategist. Your job is to choose the best action for the player {playerName}.

                Current game state: {gameState}

                Player status:
                - Name: {playerName}
                - Coins: {playerCoins}
                - Cards: {playerCards}

                Opponents:
                {opponentList}

                Choose the single best Coup action for this situation.
                Valid actions include:
                - income
                - foreign_aid
                - tax
                - steal
                - assassinate
                - exchange
                - coup

                Return ONLY a JSON object with the following fields:
                - action: the chosen Coup action
                - explanation: a short explanation of why this action is optimal
                """);

        String opponentsText = opponents.stream()
                .map(o -> "- " + o.getName() + " (coins: " + o.getCoins() + ", cards: " + o.getCards().size() + ")")
                .reduce("", (a, b) -> a + b + "\n");

        return template.create(Map.of(
                "playerName", player.getName(),
                "playerCoins", player.getCoins(),
                "playerCards", player.getCards().toString(),
                "opponentList", opponentsText,
                "gameState", state.toString()
        )).getContents();
    }
}

