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

    public Game runGame(Game game) {


        while (game.getState() != GameState.FINISHED) {

            Player player = game.getCurrentPlayer();
            AiDecision action = ai.decide(game, player, 0);
            int playerCount = game.getPlayers().size();
            String playerId= player.getId();
            int playerIndex = game.getPlayers().indexOf(player);

            if (action.action == null) {
                action.action = ActionType.INCOME;
            }
            // 1. Declare action
            game = coup.declareAction(
                    game,
                    player.getId(),
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
            //-
            //-8 Assasination


            //Counteractions
            //-2 Foreign aid by duke
            //-6 Assassination by Contessa, only from specific player
            //-4 Steal from captain if you have captain or Ambassador

            //Challengable counter Actions
            AiDecision reaction;

            //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged by anyone
            if(game.getDeclaredAction().equals(ActionType.TAX) || game.getDeclaredAction().equals(ActionType.EXCHANGE)) {


                for (int i = 1; i < playerCount; i++) {

                    Player challenger= game.getPlayers().get((playerIndex + i) % playerCount);
                    if (!challenger.isAlive() || challenger.getId().equals(playerId)) continue;

                    reaction = ai.decide(game, challenger, 1);


                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(game, challenger.getId());
                        game = coup.resolveChallenge(game);
                        break;
                    }

                }

                //Scenario 2- Foreign aid can be blocked anyone, except the original actor
            } else if (game.getDeclaredAction().equals(ActionType.FOREIGN_AID)) {


                for (int i = 1; i < playerCount; i++) {
                    Player blocker= game.getPlayers().get((playerIndex + i) % playerCount);

                    if (!blocker.isAlive() || blocker.getId().equals(playerId)) continue;
                    reaction = ai.decide(game, blocker, 2);

                    if (reaction.action == ActionType.BLOCK) {
                        game = coup.declareBlock(game, blocker.getId(), CardType.DUKE);

                        //Scenario 3- Blocked Foreign Aid can be challenged by anyone, except the original actor and blocker
                        int blocker_index = game.getPlayers().indexOf(blocker);
                        for (int j = 1; j < playerCount; j++) {
                            Player challenger = game.getPlayers().get((blocker_index + j) % playerCount);
                            if (!challenger.isAlive() ||
                                    challenger.getId().equals(blocker.getId()) ||
                                    challenger.getId().equals(player.getId())
                            ) continue;

                            reaction = ai.decide(game, challenger, 3);

                            if (reaction.action == ActionType.CHALLENGE) {
                                game = coup.declareChallenge(game,challenger.getId());
                                game = coup.resolveChallenge(game);
                                break;
                            }
                        }
                        break;
                    }
                }

                //Scenario 4- Steal can be challenged by any player, except for the target and original actor
            } else if (game.getDeclaredAction().equals(ActionType.STEAL)){

                boolean challenge_declared = false;

                for (int i = 1; i < playerCount; i++) {
                    Player challenger = game.getPlayers().get((playerIndex + i) % playerCount);

                    if (!challenger.isAlive() ||
                            challenger.getId().equals(playerId) ||
                            challenger.getId().equals(action.targetId) ) continue;

                    reaction = ai.decide(game, challenger, 4);
                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(game, challenger.getId());
                        game = coup.resolveChallenge(game);
                        challenge_declared = true;
                        break;
                    }

                }
                if (!challenge_declared){continue;}

                //Scenario 5- Steal can be blocked by targeted player if no one wants to challenge original steal
                reaction = ai.decide(game, game.getPlayer(action.targetId), 5);

                if(reaction.action == ActionType.BLOCK_USING_AMBASSADOR || reaction.action == ActionType.BLOCK_USING_CAPTAIN) {
                    Player blocker = game.getPlayer(action.targetId);
                    game = coup.declareBlock(game, blocker.getId(), roleForAction(reaction.action));

                    //Scenario 6- Block steal counteraction can be challenged by any player, except the orginal
                    int blocker_index = game.getPlayers().indexOf(blocker);
                    for (int i = 1; i < playerCount; i++) {
                        Player challenger= game.getPlayers().get((blocker_index + i) % playerCount);
                        if (!challenger.isAlive() ||
                                challenger.getId().equals(playerId) ||
                                challenger.getId().equals(blocker.getId())) continue;

                        reaction = ai.decide(game, challenger, 6);

                        if (reaction.action == ActionType.CHALLENGE) {
                            game = coup.declareChallenge(game,challenger.getId());
                            game = coup.resolveChallenge(game);
                            break;
                        }

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


