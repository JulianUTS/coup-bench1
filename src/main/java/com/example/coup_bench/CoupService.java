package com.example.coup_bench;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.repoModels.GameSummary;
import com.example.coup_bench.repo.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class CoupService {

    private final GameRepository repo;

    public CoupService(GameRepository repo) {
        this.repo = repo;
    }

    public Game createGame() {
        return new Game(UUID.randomUUID().toString());
    }

    public Game joinGame(Game game, String playerId, String provider, String personality) {
        game.addPlayer(new Player(playerId, personality));
        return game;
    }

    public Game startGame(Game game) {
        game.startGame();
        return game;
    }

    public GameSummary getGameSummary(Game game) {
        GameSummary summary = new GameSummary();

// Basic identifiers
        summary.setId(null); // MongoDB will generate this
        summary.setGameId(game.getId());

// Timestamps
        summary.setTimestampStart(game.getTimestampStart());
        summary.setTimestampEnd(System.currentTimeMillis());

// Game stats
        summary.setNumberOfPlayers(game.getPlayers().size());
        summary.setWinnerId(game.getWinnerId(game));
        summary.setTotalTurns(game.getTurn());
        summary.setTotalActions(game.getActionLog().size());
        summary.setTotalChallenges(game.getTotalBlocks());
        summary.setTotalBlocks(game.getTotalChallenges());
        summary.setTotalInvalidActions(game.getInvalidActionLog().size());

// Game memory (if you store AI thoughts or logs)
        summary.setGameMemory(new ArrayList<>(game.getGameMemory()));

// Invalid actions
        summary.setInvalidActions(new ArrayList<>(game.getInvalidActionLog()));

// Full action log
        summary.setActions(new ArrayList<>(game.getActionLog()));

// Final player states
        summary.setPlayers(game.getPlayers());

        return summary;
    }

    public Game saveIfFinished(Game game) {
        if (game.getState() == GameState.FINISHED || game.getState() == GameState.INVALID) {
            repo.save(getGameSummary(game));
        }
        return game;
    }

    public Game invalidGame(Game game) {
        game.logGameMemory("3 Invalid Actions used in a row, game is invalid");
        game.setState(GameState.INVALID);
        return saveIfFinished(game);
    }

    private Game invalidateAction(Game game, ActionRecord actionRecord, String message) {
        game.logGameMemory(actionRecord.getPlayerId() + " calls invalid " +  actionRecord.getAction() + ": " + message);
        InvalidActionRecord invalidActionRecord = new InvalidActionRecord(
                actionRecord.getPlayerId(),
                actionRecord.getAction(),
                actionRecord.getTargetId(),
                actionRecord.getDescription(),
                message
                );
        game.logInvalidAction(invalidActionRecord);
        game.incrementInvalidAction();
        return saveIfFinished(game);
    }

    public Game declareAction(Game game, ActionRecord actionRecord) {

        String playerId = actionRecord.getPlayerId();
        ActionType action = actionRecord.getAction();
        String targetId = actionRecord.getTargetId();
        Player player = game.getPlayer(playerId);


        // --- STRICT RULES ---
        // 1. Validate action legality
        if (player.getCoins() >= 10 && action != ActionType.COUP) {
            return (invalidateAction(game, actionRecord, "Must choose COUP if 10 coins or more"));
        }

        switch (action) {

            case ASSASSINATE -> {
                if (player.getCoins() < 3)
                    return invalidateAction(game, actionRecord, "Not enough coins to ASSASSINATE");
                if (targetId == null)
                    return invalidateAction(game, actionRecord, "ASSASSINATE requires a target");
                if (targetId.equals(player.getId()))
                    return invalidateAction(game, actionRecord, "Cannot ASSASSINATE yourself");
                if (!game.getPlayer(targetId).isAlive())
                    return invalidateAction(game, actionRecord, "ASSASSINATE requires an alive target");
            }
            case COUP -> {
                if (player.getCoins() < 7)
                    return invalidateAction(game, actionRecord, "Not enough coins to COUP");
                if (targetId == null)
                    return invalidateAction(game, actionRecord, "COUP requires a target");
                if (targetId.equals(player.getId()))
                    return invalidateAction(game, actionRecord, "Cannot COUP yourself");
                if (!game.getPlayer(targetId).isAlive())
                    return invalidateAction(game, actionRecord, "COUP requires an alive target");
            }

            case STEAL -> {
                if (targetId == null)
                    return invalidateAction(game, actionRecord, "STEAL requires a target");
                if (targetId.equals(player.getId()))
                    return invalidateAction(game, actionRecord, "Cannot STEAL from yourself");
                if (!game.getPlayer(targetId).isAlive())
                    return invalidateAction(game, actionRecord, "STEAL requires an alive target");
                if (game.getPlayer(targetId).getCoins() == 0)
                    return invalidateAction(game, actionRecord, "STEAL requires a target with more than 0 coins");
            }
            case TAX, FOREIGN_AID, INCOME, EXCHANGE -> {
                if (targetId != null)
                    return invalidateAction(game, actionRecord, action + " must not have a target");
            }
        }

        if(!player.hasCard(roleForAction(action))) {
            player.incrementBluffsAttempted();
        }
        game.resetInvalidAction();
        game.declareAction(actionRecord);
        return saveIfFinished(game);
    }

    public Game declareBlock(Game game, String blockerId, AiReaction aiReaction) {

        game.setState(GameState.BLOCK_DECLARED);
        game.setBlockerId(blockerId);
        game.setBlockingRole(roleForAction(aiReaction.action));
        game.logGameMemory(blockerId + " declares " + aiReaction.action + " on " + game.getActingPlayerId());
        game.incrementTotalBlocks();

        game.getPlayer(blockerId).incrementBlocksIssued();
        if (!game.getPlayer(blockerId).hasCard(game.getBlockingRole())){
            game.getPlayer(blockerId).incrementBluffsAttempted();
        }

        game.logAction(new ActionRecord(
                blockerId,
                aiReaction.action,
                game.getActingPlayerId(),
                aiReaction.reason
        ));

        return saveIfFinished(game);
    }


    public Game declareChallenge(Game game, String challengerId, AiReaction aiReaction) {

        game.setState(GameState.CHALLENGE_DECLARED);
        game.setChallengerId(challengerId);
        String targetId = game.getBlockerId() != null ? game.getBlockerId() : game.getActingPlayerId();
        game.logGameMemory(challengerId + " declares " + aiReaction.action + " on " + targetId);
        game.incrementTotalChallenges();

        game.getPlayer(challengerId).incrementChallengesIssued();

        game.logAction(new ActionRecord(
                challengerId,
                ActionType.CHALLENGE,
                targetId,
                aiReaction.reason
        ));

        return saveIfFinished(game);
    }

    public CardType chooseCard(Game game, Player player, AiDecisionService ai) {
        if(player.getCards().size() == 1){
            return player.getCards().getFirst();
        }
        return ai.getCardToLoose(game, player);

    }


    public Game resolveChallenge(Game game, AiDecisionService ai) {
        //Challenge the block
        Player challenger = game.getPlayer(game.getChallengerId());
        if (game.getBlockerId() != null) {
            Player blocker = game.getPlayer(game.getBlockerId());
            if(blocker.hasCard(game.getBlockingRole())){
                //Challenge is unsuccessful, Blocker switches card, Challenger Loses Card
                game.logGameMemory(blocker.getId() + " wins challenge");

                challenger.incrementChallengesLost();

                CardType cardToLoose = chooseCard(game, challenger, ai);
                game.switchCard(blocker.getId(), game.getBlockingRole());
                game.removeCard(challenger.getId(), cardToLoose);
                game.setState(GameState.IN_PROGRESS);
            } else{
                //Challenge is successful, blocker looses card, orginal action is applied
                game.logGameMemory(challenger.getId() + " wins challenge");

                blocker.incrementBluffsFailed();
                blocker.incrementBlocksFailed();
                challenger.incrementChallengesWon();

                CardType cardToLoose = chooseCard(game, blocker, ai);
                game.removeCard(blocker.getId(), cardToLoose);
                game.setState(GameState.APPLYING_ACTION);
            }
            //Challenge the action
        } else{
            Player player = game.getPlayer(game.getActingPlayerId());
            CardType challengedCard = roleForAction(game.getDeclaredAction());
            //Challenge is unsuccessful, Player switches card, Challenger Loses Card, orginal action is applied
            if(player.hasCard(challengedCard)) {
                game.logGameMemory(player.getId() + " wins challenge");

                challenger.incrementChallengesLost();

                CardType cardToLoose = chooseCard(game, challenger, ai);
                game.switchCard(player.getId(), challengedCard);
                game.removeCard(challenger.getId(), cardToLoose);
                game.setState(GameState.APPLYING_ACTION);
            } else{
                //Challenge is successful, player looses card, orginal action is invalid
                game.logGameMemory(challenger.getId() + " wins challenge");

                player.incrementBluffsFailed();
                challenger.incrementChallengesWon();

                CardType cardToLoose = chooseCard(game, player, ai);
                game.removeCard(player.getId(), cardToLoose);
                game.setState(GameState.IN_PROGRESS);
            }
        }
        game.clearChallengeData();
        return saveIfFinished(game);
    }

    public Game applyAction(Game game, AiDecisionService ai) {

        if (game.getState() != GameState.APPLYING_ACTION &&
                game.getState() != GameState.ACTION_DECLARED)
            throw new IllegalStateException("No action to apply");

        ActionType action = game.getDeclaredAction();
        Player player = game.getPlayer(game.getActingPlayerId());
        Player target = game.getTargetId() != null
                ? game.getPlayer(game.getTargetId())
                : null;

        if(!player.hasCard(roleForAction(action))){
            player.incrementBluffsSuccessful();
        }

        switch (action) {
            case INCOME -> {
                player.addCoins(1);
                player.incrementIncomeCount();
                game.logGameMemory(player.getId() + " gains 1 coin (INCOME)");
            }

            case FOREIGN_AID -> {
                player.addCoins(2);
                game.logGameMemory(player.getId() + " gains 2 coins (FOREIGN AID)");
            }

            case TAX -> {
                player.incrementTaxCount();
                player.addCoins(3);
                game.logGameMemory(player.getId() + " gains 3 coins (DUKE TAX)");
            }

            case STEAL -> {
                player.incrementStealSuccesses();
                int stolen = Math.min(2, target.getCoins());
                target.removeCoins(stolen);
                player.addCoins(stolen);
                game.logGameMemory(player.getId() + " steals " + stolen + " coins from " + target.getId());
            }

            case ASSASSINATE -> {
                player.removeCoins(3);
                game.removeCard(player.getId(), chooseCard(game, player, ai));
                game.logGameMemory(player.getId() + " assassinates " + target.getId());
            }

            case COUP -> {
                player.removeCoins(7);
                game.removeCard(target.getId(), chooseCard(game, target, ai));
                player.incrementCoupsPerformed();
            }

            case EXCHANGE -> {
                // Minimal version: draw 2 cards and keep both
                CardType c1 = game.drawCard();
                CardType c2 = game.drawCard();
                player.addCard(c1);
                player.addCard(c2);

                game.logGameMemory(player.getId() + " exchanges cards");
            }
        }


        return saveIfFinished(game);
    }

    public Game nextTurn(Game game){
        game.nextTurn();
        if (game.getState() != GameState.FINISHED) {
            game.setState(GameState.IN_PROGRESS);
        }
        return saveIfFinished(game);
    };

    public Game applyBlock(Game game){
        game.getPlayer(game.getBlockerId()).incrementBlocksSuccessful();
        if(!game.getPlayer(game.getBlockerId()).hasCard(game.getBlockingRole())){
            game.getPlayer(game.getBlockerId()).incrementBluffsSuccessful();

        }
        return saveIfFinished(game);
    };


    private CardType roleForAction(ActionType action) {
        return switch (action) {
            case TAX -> CardType.DUKE;
            case STEAL -> CardType.CAPTAIN;
            case ASSASSINATE -> CardType.ASSASSIN;
            case EXCHANGE -> CardType.AMBASSADOR;
            case BLOCK_USING_AMBASSADOR -> CardType.AMBASSADOR;
            case BLOCK_USING_CAPTAIN -> CardType.CAPTAIN;
            case BLOCK_USING_CONTESSA -> CardType.CONTESSA;
            case BLOCK_USING_DUKE ->  CardType.DUKE;

            default -> null;
        };
    }
}