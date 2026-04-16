package com.example.coup_bench;

import com.example.coup_bench.model.*;
import org.springframework.stereotype.Service;


@Service
public class AiGameRunner {

    private final CoupService coup;
    private final AiDecisionService ai;

    public AiGameRunner(CoupService coup, AiDecisionService ai) {
        this.coup = coup;
        this.ai = ai;
    }

    public Game runGame(String gameId) {

        Game game = coup.getGame(gameId);

        while (game.getState() != GameState.FINISHED) {

            Player current = game.getCurrentPlayer();
            AiDecision decision = ai.decide(game, current);

            if (decision.action == null) {
                decision.action = ActionType.INCOME;
            }
            // 1. Declare action
            game = coup.declareAction(
                    gameId,
                    current.getId(),
                    decision.action,
                    decision.targetId
            );

            // 2. Other players may block or challenge
            for (Player p : game.getPlayers()) {
                if (!p.isAlive() || p.getId().equals(current.getId())) continue;

                AiDecision reaction = ai.decide(game, p);

                if (reaction.block) {
                    game = coup.declareBlock(gameId, p.getId(), roleForAction(decision.action));
                }

                if (reaction.challenge) {
                    game = coup.declareChallenge(gameId, p.getId());
                    game = coup.resolveChallenge(gameId);
                }
            }

            // 3. Apply action if still valid
            if (game.getState() == GameState.APPLYING_ACTION ||
                    game.getState() == GameState.ACTION_DECLARED) {
                game = coup.applyAction(gameId);
            }
        }

        return game;
    }

    private CardType roleForAction(ActionType action) {
        return switch (action) {
            case TAX -> CardType.DUKE;
            case STEAL -> CardType.CAPTAIN;
            case ASSASSINATE -> CardType.ASSASSIN;
            case EXCHANGE -> CardType.AMBASSADOR;
            default -> null;
        };
    }
}


