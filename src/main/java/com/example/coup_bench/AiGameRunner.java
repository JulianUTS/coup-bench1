package com.example.coup_bench;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiAction;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.service.AiDecisionService;
import com.example.coup_bench.service.CoupService;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;


@Service
public class AiGameRunner {

    private final CoupService coup;
    private final AiDecisionService ai;
    private final PlayerHelperService playerHelperService;


    public AiGameRunner(CoupService coup, AiDecisionService ai, PlayerHelperService playerHelperService) {
        this.coup = coup;
        this.ai = ai;
        this.playerHelperService = playerHelperService;
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

               // System.out.println(actionRecord.getDescription());
                game = coup.declareAction(
                        game,
                        playerId,
                        action.action,
                        action.targetId,
                        action.reason
                );
            }

            //2. Check if game is invalid
            if(game.getInvalidAction() == 3){
                game = coup.invalidGame(game);
                coup.saveIfFinished(game);
                return game;
            }

            /*
            Game scenarios:
            1- Tax/Exchange
            Action can be challenged by anyone, no block

            2- Foreign aid
            Action can be blocked by anyone, blocked can be challenged by anyone

            3- Steal
            Action can be challenged by anyone except for targeted player, action can be blocked by targeted player,
            counter can be challenged by anyone except for target and original player

            4- Assassination
            Action can be challenged by anyone, action can be blocked by targeted player,
            counter can be challenged by anyone except for target and original player
             */
            ActionType declared = game.getDeclaredAction();
            String targetId = game.getTargetId();

            //1
            if (declared == ActionType.TAX || declared == ActionType.EXCHANGE) {
                game = Scenario1(game, playerIndex, playerCount, playerId);
            }
            //2
            if (declared == ActionType.FOREIGN_AID) {
                game = Scenario2(game, playerIndex, playerCount, playerId);
            }

            //3
            if (declared == ActionType.STEAL) {
                game = Scenario3(game, playerIndex, playerCount, playerId, targetId);
            }

            //4
            if (declared == ActionType.ASSASSINATE) {
                game = Scenario4(game, playerIndex, playerCount, playerId, targetId);
            };


            // 3. Apply action if still valid
            if (game.getState() == GameState.APPLYING_ACTION || game.getState() == GameState.ACTION_DECLARED) {
                game = coup.applyAction(game);
            } else if(game.getState() == GameState.BLOCK_DECLARED){
                game = coup.applyBlock(game);
            }

            game = coup.nextTurn(game);
        }
        return game;
    }

    private Game findChallenger(Game game, int startIndex, int playerCount, Predicate<Player> filter, int reactionCode) {

        for (int i = 1; i < playerCount; i++) {
            Player p = game.getPlayers().get((startIndex + i) % playerCount);
            if (!filter.test(p)) continue;

            AiReaction reaction = ai.getReaction(game, p, reactionCode);

            if (reaction.action == ActionType.CHALLENGE) {
                game = coup.declareChallenge(game, p.getId(), reaction);
                game = coup.resolveChallenge(game, ai);
                return game;
            } else if(reaction.action == ActionType.BLOCK_USING_DUKE){
                game = coup.declareBlock(game, p.getId(), reaction);
                return game;
            } else{
                game = coup.logAction(game, new ActionRecord(p.getId(), reaction.action, null, reaction.reason));
            }
        }
        return game;
    }

    private Game attemptBlock(Game game, Player blocker, int reactionCode, int playerCount, String playerId) {
        AiReaction r = ai.getReaction(game, blocker, reactionCode);

        if (r.action == ActionType.DO_NOTHING) return game;

        game = coup.declareBlock(game, blocker.getId(), r);

        //Challenge block
        game = findChallenger(
                game, game.getPlayers().indexOf(blocker), playerCount,
                p -> p.isAlive() &&
                        !p.getId().equals(playerId) &&
                        !p.getId().equals(blocker.getId()),
                3
        );

        return game;
    }

    private Game Scenario1(Game game, int playerIndex,  int playerCount, String playerId) {
            game = findChallenger(
                    game, playerIndex, playerCount,
                    p -> p.isAlive() && !p.getId().equals(playerId),
                    1
            );
            return game;
    };

    private Game Scenario2(Game game, int playerIndex,  int playerCount, String playerId) {
        game = findChallenger(
                game, playerIndex, playerCount,
                p -> p.isAlive() && !p.getId().equals(playerId),
                2
        );



        if (game.getBlockerId() != null) {
            Player blocker = game.getPlayer(game.getBlockerId());
            game = findChallenger(
                    game, game.getPlayers().indexOf(blocker), playerCount,
                    p -> p.isAlive() &&
                            !p.getId().equals(blocker.getId()) &&
                            !p.getId().equals(playerId),
                    3
            );
        }
        return game;
    };

    private Game Scenario3(Game game, int playerIndex,  int playerCount, String playerId, String targetId) {
        // 1. Challenge original steal
         game = findChallenger(
                game, playerIndex, playerCount,
                p -> p.isAlive() &&
                        !p.getId().equals(playerId) &&
                        !p.getId().equals(targetId),
                4
        );
        if (game.getChallengerId() != null) return game;

        // 2. Block by target
        Player target = game.getPlayer(targetId);
        game = attemptBlock(game, target, 5, playerCount, playerId);
        return game;

    };

    private Game Scenario4(Game game, int playerIndex,  int playerCount, String playerId, String targetId) {
        // 1. Challenge assassinate
        game = findChallenger(
                game, playerIndex, playerCount,
                p -> p.isAlive() &&
                        !p.getId().equals(playerId),
                6
        );
        if (game.getChallengerId() != null) return game;

        // 2. Block by target
        Player target = game.getPlayer(targetId);
        game = attemptBlock(game, target, 7, playerCount, playerId);
        return game;

    };
}


