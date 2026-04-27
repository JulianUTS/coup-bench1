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
            AiDecision action = ai.decide(game, current, 0);

            if (action.action == null) {
                action.action = ActionType.INCOME;
            }
            // 1. Declare action
            game = coup.declareAction(
                    game,
                    current.getId(),
                    action.action,
                    action.targetId
            );

            // 2. Other players may block or challenge
            //Scenarios
            //Challengable Action
            //-1 Tax from duke & Exchange Cards from ambassador
            //-5 Steal from captain
            //-3 Blocked Foreign aid by duke
            //-5 Blocked Steal
            //-7 Blocked assassination
            //-8 Assasination


            //Counteractions
            //-2 Foreign aid by duke
            //-6 Assassination by Contessa, only from specific player
            //-4 Steal from captain if you have captain or Ambassador

            //Challengable counter Actions
            AiDecision reaction;

            //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged by anyone
            if(game.getDeclaredAction().equals(ActionType.TAX) || game.getDeclaredAction().equals(ActionType.EXCHANGE)) {

                boolean challenge_declared = false;
                int current_player_ind= game.getPlayers().indexOf(current);
                for (int i = 1; i < game.getPlayers().size(); i++) {

                    Player reacting_player= game.getPlayers().get((current_player_ind + i) % game.getPlayers().size());
                    if (!reacting_player.isAlive() || reacting_player.getId().equals(current.getId())) continue;

                    reaction = ai.decide(game, reacting_player, 1);


                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(game, reacting_player.getId());
                        game = coup.resolveChallenge(game);
                        challenge_declared = true;
                    }
                    if (challenge_declared){break;}
                }

                //Scenario 2- Foreign aid can be blocked anyone
            } else if (game.getDeclaredAction().equals(ActionType.FOREIGN_AID)) {

                boolean challenge_declared = false;
                int current_player_ind= game.getPlayers().indexOf(current);
                for (int i = 1; i < game.getPlayers().size(); i++) {
                    Player reacting_player= game.getPlayers().get((current_player_ind + i) % game.getPlayers().size());

                    if (!reacting_player.isAlive() || reacting_player.getId().equals(current.getId())) continue;
                    reaction = ai.decide(game, reacting_player, 2);

                    if (reaction.action == ActionType.BLOCK) {
                        challenge_declared = true;
                        game = coup.declareBlock(game, reacting_player.getId(), CardType.DUKE);

                        //Scenario 3- Blocked Foreign Aid can be challenged
                        reaction = ai.decide(game, current, 3);
                        if (reaction.action == ActionType.CHALLENGE) {
                            game = coup.declareChallenge(game, current.getId());
                            game = coup.resolveChallenge(game);
                        }

                    }

                    if (challenge_declared){break;}
                }

                //Scenario 4- Steal can be challenged by any player
            } else if (game.getDeclaredAction().equals(ActionType.STEAL)){

                boolean challenge_declared = false;

                int current_player_ind= game.getPlayers().indexOf(current);
                for (int i = 1; i < game.getPlayers().size(); i++) {
                    Player reacting_player = game.getPlayers().get((current_player_ind + i) % game.getPlayers().size());

                    if (!reacting_player.isAlive() || reacting_player.getId().equals(current.getId())) continue;
                    reaction = ai.decide(game, reacting_player, 4);
                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(game, reacting_player.getId());
                        game = coup.resolveChallenge(game);
                        challenge_declared = true;
                    }
                    if (challenge_declared){break;}
                }
                if (challenge_declared){break;}

                //Scenario 5- Steal can be blocked by targeted player if no one wants to challenge original steal
                reaction = ai.decide(game, game.getPlayer(action.targetId), 5);

                if(reaction.action == ActionType.BLOCK_USING_AMBASSADOR || reaction.action == ActionType.BLOCK_USING_CAPTAIN) {
                    game = coup.declareBlock(game, action.targetId, roleForAction(reaction.action));

                    //Scenario 6- Block steal counteraction can be challenged by any player
                    int reacting_player_ind= game.getPlayers().indexOf(game.getPlayer(action.targetId));
                    for (int i = 1; i < game.getPlayers().size(); i++) {
                        Player reacting_player= game.getPlayers().get((reacting_player_ind + i) % game.getPlayers().size());
                        if (!reacting_player.isAlive() || reacting_player.getId().equals(current.getId())) continue;

                        reaction = ai.decide(game, reacting_player, 6);

                        if (reaction.action == ActionType.CHALLENGE) {
                            game = coup.declareChallenge(game,reacting_player.getId());
                            game = coup.resolveChallenge(game);
                            challenge_declared = true;
                        }
                        if (challenge_declared){break;}
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
            case BLOCK_USING_AMBASSADOR -> CardType.AMBASSADOR;
            case BLOCK_USING_CAPTAIN -> CardType.CAPTAIN;

            default -> null;
        };
    }
}


