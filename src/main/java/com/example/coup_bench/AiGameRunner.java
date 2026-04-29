package com.example.coup_bench;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiReaction;
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
            int playerCount = game.getPlayers().size();
            String playerId= player.getId();
            int playerIndex = game.getPlayers().indexOf(player);


            // 1. Declare action
            while(game.getState() == GameState.IN_PROGRESS && game.getInvalidAction() < 3) {
                AiAction action = ai.getAction(game, player);
                ActionRecord actionRecord = new ActionRecord(player.getId(), action.action, action.targetId, action.reason);
                System.out.println(actionRecord.getDescription());
                game = coup.declareAction(
                        game,
                        actionRecord
                );

            }

            if(game.getInvalidAction() == 3){
                game = coup.invalidGame(game);
                coup.saveIfFinished(game);

                return game;

            }




            // 2. Other players may block or challenge
            //Scenarios
            //Challengeable
            //-1 Tax from duke & Exchange Cards from ambassador
            //-3 Block foreign aid counteraction
            //-4 Steal
            //-6 Block Steal counteraction
            //-7 Assassinate
            //-9 Block Assassinate counteraction

            //Blocks
            //-2 Foreign aid
            // 5 Steal
            //-8 Assassinate


            AiReaction reaction;


            //Scenario 1- Tax from duke & Exchange Cards from ambassador can be challenged by anyone
            if(game.getDeclaredAction().equals(ActionType.TAX) || game.getDeclaredAction().equals(ActionType.EXCHANGE)) {


                for (int i = 1; i < playerCount; i++) {

                    Player challenger= game.getPlayers().get((playerIndex + i) % playerCount);
                    if (!challenger.isAlive() || challenger.getId().equals(playerId)) continue;

                    reaction = ai.getReaction(game, challenger, 1);


                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(game, challenger.getId(), reaction);
                        game = coup.resolveChallenge(game, ai);
                        break;
                    }

                }

                //Scenario 2- Foreign aid can be blocked anyone, except the original actor
            } else if (game.getDeclaredAction().equals(ActionType.FOREIGN_AID)) {


                for (int i = 1; i < playerCount; i++) {
                    Player blocker= game.getPlayers().get((playerIndex + i) % playerCount);

                    if (!blocker.isAlive() || blocker.getId().equals(playerId)) continue;
                    reaction = ai.getReaction(game, blocker, 2);

                    if (reaction.action != ActionType.DO_NOTHING) {
                        game = coup.declareBlock(game, blocker.getId(), reaction);

                        //Scenario 3- Blocked Foreign Aid can be challenged by anyone, except the original actor and blocker
                        int blocker_index = game.getPlayers().indexOf(blocker);
                        for (int j = 1; j < playerCount; j++) {
                            Player challenger = game.getPlayers().get((blocker_index + j) % playerCount);
                            if (!challenger.isAlive() ||
                                    challenger.getId().equals(blocker.getId()) ||
                                    challenger.getId().equals(player.getId())
                            ) continue;

                            reaction = ai.getReaction(game, challenger, 3);

                            if (reaction.action == ActionType.CHALLENGE) {
                                game = coup.declareChallenge(game,challenger.getId(), reaction);
                                game = coup.resolveChallenge(game, ai);
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
                            challenger.getId().equals(game.getTargetId()) ) continue;

                    reaction = ai.getReaction(game, challenger, 4);
                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(game, challenger.getId(), reaction);
                        game = coup.resolveChallenge(game, ai);
                        challenge_declared = true;
                        break;
                    }

                }
                if (challenge_declared) {
                    break;
                }

                //Scenario 5- Steal can be blocked by targeted player if no one wants to challenge original steal
                reaction = ai.getReaction(game, game.getPlayer(game.getTargetId()), 5);

                if(reaction.action != ActionType.DO_NOTHING) {
                    Player blocker = game.getPlayer(game.getTargetId());
                    game = coup.declareBlock(game, blocker.getId(), reaction);

                    //Scenario 6- Block steal counteraction can be challenged by any player, except the orginal
                    int blocker_index = game.getPlayers().indexOf(blocker);
                    for (int i = 1; i < playerCount; i++) {
                        Player challenger= game.getPlayers().get((blocker_index + i) % playerCount);
                        if (!challenger.isAlive() ||
                                challenger.getId().equals(playerId) ||
                                challenger.getId().equals(blocker.getId())) continue;

                        reaction = ai.getReaction(game, challenger, 3);

                        if (reaction.action == ActionType.CHALLENGE) {
                            game = coup.declareChallenge(game,challenger.getId(), reaction);
                            game = coup.resolveChallenge(game, ai);
                            break;
                        }

                    }
                }
                //Scenario 7- Assassinate can be challenged by any player, except for the original actor
            } else if (game.getDeclaredAction().equals(ActionType.ASSASSINATE)){

                boolean challenge_declared = false;

                for (int i = 1; i < playerCount; i++) {
                    Player challenger = game.getPlayers().get((playerIndex + i) % playerCount);

                    if (!challenger.isAlive() ||
                            challenger.getId().equals(playerId) ||
                            challenger.getId().equals(game.getTargetId()) ) continue;

                    reaction = ai.getReaction(game, challenger, 6);
                    if (reaction.action == ActionType.CHALLENGE) {
                        game = coup.declareChallenge(game, challenger.getId(), reaction);
                        game = coup.resolveChallenge(game, ai);
                        challenge_declared = true;
                        break;
                    }

                }
                if (challenge_declared) {
                    break;
                }

                //Scenario 8- Assassinate can be blocked by targeted player if no one wants to challenge original assassinate
                reaction = ai.getReaction(game, game.getPlayer(game.getTargetId()), 7);

                if(reaction.action != ActionType.DO_NOTHING) {
                    Player blocker = game.getPlayer(game.getTargetId());
                    game = coup.declareBlock(game, blocker.getId(), reaction);

                    //Scenario 9- Block assassinate counteraction can be challenged by any player, except the original
                    int blocker_index = game.getPlayers().indexOf(blocker);
                    for (int i = 1; i < playerCount; i++) {
                        Player challenger= game.getPlayers().get((blocker_index + i) % playerCount);
                        if (!challenger.isAlive() ||
                                challenger.getId().equals(playerId) ||
                                challenger.getId().equals(blocker.getId())) continue;

                        reaction = ai.getReaction(game, challenger, 3);

                        if (reaction.action == ActionType.CHALLENGE) {
                            game = coup.declareChallenge(game,challenger.getId(),reaction);
                            game = coup.resolveChallenge(game, ai);
                            break;
                        }

                    }
                }
            }


            // 3. Apply action if still valid
            if (game.getState() == GameState.APPLYING_ACTION || game.getState() == GameState.ACTION_DECLARED) {
                game = coup.applyAction(game, ai);
            }

            game = coup.nextTurn(game);
        }
        coup.saveIfFinished(game);

        return game;
    }


}


