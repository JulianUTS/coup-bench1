package com.example.coup_bench;

import com.example.coup_bench.model.*;
import org.springframework.stereotype.Service;

import java.util.List;


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
            AiDecision decision = ai.decide(game, current, 0);

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
            //Scenarios
            //Challengable Action
            //- 1 Tax from duke & Exchange Cards from ambassador
            //-  Steal from captain
            //-3 Blocked Foreign aid by duke


            //Blockable Actions
            //-2 -Foreign aid by duke
            //-Assassination by Contessa, only from specific player
            //-Steal from captain if you have captain or Ambassador


            //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged
            if(game.getDeclaredAction().equals(ActionType.TAX) || game.getDeclaredAction().equals(ActionType.EXCHANGE)) {
                for (Player p : game.getPlayers()) {
                    AiDecision reaction = ai.decide(game, p, 1);
                    if (!p.isAlive() || p.getId().equals(current.getId())) continue;

                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(gameId, p.getId());
                        game = coup.resolveChallenge(gameId);
                        break;
                    }
                }
                //Scenario 2- Foreign aid can be blocked
            } else if (game.getDeclaredAction().equals(ActionType.FOREIGN_AID)) {
                for (Player p : game.getPlayers()) {
                    AiDecision reaction = ai.decide(game, p, 2);
                    if (!p.isAlive() || p.getId().equals(current.getId())) continue;

                    if (reaction.action == ActionType.BLOCK) {
                        game = coup.declareBlock(gameId, p.getId(), roleForAction(decision.action));

                        //Scenario 3- Blocked Foreign Aid can be challenged
                        reaction = ai.decide(game, current, 3);
                        if (reaction.action == ActionType.CHALLENGE) {
                            game = coup.declareChallenge(gameId, current.getId());
                            game = coup.resolveChallenge(gameId);
                        }
                    }
                }
            }


            // 3. Apply action if still valid
            if (game.getState() == GameState.APPLYING_ACTION || game.getState() == GameState.ACTION_DECLARED) {
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


