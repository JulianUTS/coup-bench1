package com.example.coup_bench;

import com.example.coup_bench.model.*;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;


@Service
public class AiGameRunner {

    private final CoupService coup;
    private final AiDecisionService ai;


    public AiGameRunner(CoupService coup, AiDecisionService ai) {
        this.coup = coup;
        this.ai = ai;
    }

    public Game runGame(Game game) {


        while (game.getState() != GameState.FINISHED) {

            Player current = game.getCurrentPlayer();
            AiDecision decision = ai.decide(game, current, 0);

            if (decision.action == null) {
                decision.action = ActionType.INCOME;
            }
            // 1. Declare action
            game = coup.declareAction(
                    game,
                    current.getId(),
                    decision.action,
                    decision.targetId
            );

            // 2. Other players may block or challenge
            //Scenarios
            //Challengable Action
            //- 1 Tax from duke & Exchange Cards from ambassador
            //- 4 Steal from captain
            //-3 Blocked Foreign aid by duke


            //Blockable Actions
            //-2 -Foreign aid by duke
            //-Assassination by Contessa, only from specific player
            //-Steal from captain if you have captain or Ambassador


            //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged
            if(game.getDeclaredAction().equals(ActionType.TAX) || game.getDeclaredAction().equals(ActionType.EXCHANGE)) {
                boolean challenge_declared = false;
                List<Player> players = game.getPlayers();
                Iterator<Player> player = players.iterator();
                while (player.hasNext() && !challenge_declared) {

                    Player p = player.next();
                    if (!p.isAlive() || p.getId().equals(current.getId())) continue;
                    AiDecision reaction = ai.decide(game, p, 1);


                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(game, p.getId());
                        game = coup.resolveChallenge(game);
                        challenge_declared = true;
                    }
                }

                //Scenario 2- Foreign aid can be blocked
            } else if (game.getDeclaredAction().equals(ActionType.FOREIGN_AID)) {
                boolean challenge_declared = false;
                List<Player> players = game.getPlayers();
                Iterator<Player> player = players.iterator();
                while (player.hasNext() && !challenge_declared) {
                    Player p = player.next();
                    if (!p.isAlive() || p.getId().equals(current.getId())) continue;
                    AiDecision reaction = ai.decide(game, p, 2);

                    if (reaction.action == ActionType.BLOCK) {
                        game = coup.declareBlock(game, p.getId(), CardType.DUKE);

                        //Scenario 3- Blocked Foreign Aid can be challenged
                        reaction = ai.decide(game, current, 3);
                        if (reaction.action == ActionType.CHALLENGE) {
                            game = coup.declareChallenge(game, current.getId());
                            game = coup.resolveChallenge(game);
                        }
                        challenge_declared = true;
                    }
                }
                //Scenario 4- Steal can be blocked/challenged
            } else if (game.getDeclaredAction().equals(ActionType.STEAL)){
                boolean challenge_declared = false;
                List<Player> players = game.getPlayers();
                Iterator<Player> player = players.iterator();
                while (player.hasNext() && !challenge_declared) {
                    Player p = player.next();
                    if (!p.isAlive() || p.getId().equals(current.getId())) continue;
                    AiDecision reaction = ai.decide(game, p, 4);

                    //Scenario 5-If blocked, Attacking player can challenge blocking player
                    if (reaction.action == ActionType.BLOCK) {
                        game = coup.declareBlock(game, p.getId(), roleForAction(decision.action));

                }
                    }

        }


            // 3. Apply action if still valid
            if (game.getState() == GameState.APPLYING_ACTION || game.getState() == GameState.ACTION_DECLARED) {
                game = coup.applyAction(game);
            }
        }
        coup.saveIfFinished(game);

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


