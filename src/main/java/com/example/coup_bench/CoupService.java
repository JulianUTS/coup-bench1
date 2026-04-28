package com.example.coup_bench;

import com.example.coup_bench.model.*;
import com.example.coup_bench.repo.GameRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CoupService {

    private final GameRepository repo;

    public CoupService(GameRepository repo) {
        this.repo = repo;
    }

    public Game createGame() {
        Game game = new Game(UUID.randomUUID().toString());
        return new Game(UUID.randomUUID().toString());
    }

    public Game getGame(String gameId) {
        return repo.find(gameId);
    }

    public Game joinGame(Game game, String playerId, String provider, String personality) {
        game.addPlayer(new Player(playerId, provider, personality));
        return game;
    }

    public Game startGame(Game game) {
        game.startGame();
        return game;
    }

    public Game saveIfFinished(Game game) {
        if (game.getState() == GameState.FINISHED) {
            repo.save(game);
        }
        return game;
    }

    public Game invalidGame(Game game) {
        game.logGameMemory("3 Valid Actions used in a row, game is invalid");
        repo.save(game);
        return game;
    }

    public Game declareAction(Game game, ActionRecord actionRecord) {
        String playerId = actionRecord.getPlayerId();
        ActionType action = actionRecord.getAction();
        String targetId = actionRecord.getTargetId();

        Player actor = game.getPlayer(playerId);
        boolean valid = false;

        // --- STRICT RULES ---
        // 1. Validate action legality
        if (actor.getCoins() >= 10 && action != ActionType.COUP) {
            throw new IllegalStateException("Must choose COUP if 10 coins or more");
        }

        // Actions that must NOT have a target
        if (targetId != null && switch (action) {
            case INCOME, FOREIGN_AID, TAX, EXCHANGE -> true;
            default -> false;
        }) {
            throw new IllegalStateException(action + " must not have a target");
        }

        switch (action) {
            case ASSASSINATE -> {
                if (actor.getCoins() < 3)
                    throw new IllegalStateException("Not enough coins to ASSASSINATE");
                if (targetId == null)
                    throw new IllegalStateException("ASSASSINATE requires a target");
                if (targetId.equals(actor.getId()))
                    throw new IllegalStateException("Cannot ASSASSINATE yourself");
                if (!game.getPlayer(targetId).isAlive())
                    throw new IllegalStateException("ASSASSINATE requires an alive target");
            }
            case COUP -> {
                if (actor.getCoins() < 7)
                    throw new IllegalStateException("Not enough coins to COUP");
                if (targetId == null)
                    throw new IllegalStateException("COUP requires a target");
                if (targetId.equals(actor.getId()))
                    throw new IllegalStateException("Cannot COUP yourself");
                if (!game.getPlayer(targetId).isAlive())
                    throw new IllegalStateException("COUP requires an alive target");
            }
            case STEAL -> {
                if (targetId == null)
                    throw new IllegalStateException("STEAL requires a target");
                if (targetId.equals(actor.getId()))
                    throw new IllegalStateException("Cannot STEAL from yourself");
                if (game.getPlayer(targetId).getCoins() == 0)
                    throw new IllegalStateException("STEAL requires a target with more than 0 coins");
                if (!game.getPlayer(targetId).isAlive())
                    throw new IllegalStateException("STEAL requires an alive target");
            }
        }

        if(targetId == null){
            game.logGameMemory(playerId + " used " + action);
        } else{
            game.logGameMemory(playerId + " used " + action + " on " + targetId);
        }

        game.declareAction(playerId, action, targetId);
        return saveIfFinished(game);
    }

    public Game declareBlock(Game game, String blockingPlayerId, CardType claimedRole) {

        // Cannot block INCOME
        if (game.getDeclaredAction() == ActionType.INCOME)
            throw new IllegalStateException("Cannot block INCOME");

        // Cannot block COUP
        if (game.getDeclaredAction() == ActionType.COUP)
            throw new IllegalStateException("Cannot block COUP");

        game.setState(GameState.BLOCK_DECLARED);
        game.setBlockingPlayerId(blockingPlayerId);
        game.setBlockingRole(claimedRole);

        game.logAction(new ActionRecord(
                blockingPlayerId,
                ActionType.BLOCK,
                game.getActingPlayerId(),
                blockingPlayerId + " blocks " + game.getDeclaredAction() + " claiming " + claimedRole
        ));

        return saveIfFinished(game);
    }


    public Game declareChallenge(Game game, String challengerId) {


        // Cannot challenge INCOME
        if (game.getDeclaredAction() == ActionType.INCOME)
            throw new IllegalStateException("Cannot challenge INCOME");

        game.setState(GameState.CHALLENGE_PENDING);
        game.setChallengerId(challengerId);

        game.logAction(new ActionRecord(
                challengerId,
                ActionType.CHALLENGE,
                game.getBlockingPlayerId() != null ? game.getBlockingPlayerId() : game.getActingPlayerId(),
                challengerId + " challenges the claim"
        ));

        return saveIfFinished(game);
    }


    public Game resolveChallenge(Game game) {

        boolean challengeOnBlock = game.getState() == GameState.BLOCK_DECLARED
                || (game.getState() == GameState.CHALLENGE_PENDING && game.getBlockingPlayerId() != null);

        String challengedPlayerId = challengeOnBlock
                ? game.getBlockingPlayerId()
                : game.getActingPlayerId();

        Player challenged = game.getPlayer(challengedPlayerId);
        Player challenger = game.getPlayer(game.getChallengerId());

        CardType claimedRole = challengeOnBlock
                ? game.getBlockingRole()
                : roleForAction(game.getDeclaredAction());

        boolean challengedHasRole = challenged.getCards().stream()
                .anyMatch(c -> c.getType() == claimedRole && !c.isRevealed());

        if (challengedHasRole) {
            // Challenger loses

            game.logAction(new ActionRecord(
                    challenger.getId(),
                    ActionType.CHALLENGE,
                    challenged.getId(),
                    challenger.getId() + " loses challenge against " + challenged.getId()
            ));
            Card lost = challenger.removeAnyCard();
            game.logAction(new ActionRecord(
                    challenger.getId(),
                    ActionType.LOSE_CARD,
                    null,
                    challenger.getId() + " loses a card (" + lost.getType() + ")"
            ));

            // Challenged swaps that role
            challenged.getCards().stream()
                    .filter(c -> c.getType() == claimedRole && !c.isRevealed())
                    .findFirst()
                    .ifPresent(card -> {
                        card.reveal();
                        game.discard(card);
                        challenged.addCard(game.drawCard());
                    });
            game.logAction(new ActionRecord(
                    challenged.getId(),
                    ActionType.EXCHANGE,
                    null,
                    challenged.getId() + " proves " + claimedRole + " and draws a replacement"
            ));

            // If challenge was on block, action is blocked; otherwise action proceeds
            game.setState(challengeOnBlock ? GameState.IN_PROGRESS : GameState.APPLYING_ACTION);
        } else {
            // Challenged loses
            challenged.removeAnyCard();
            // If challenged was actor, action fails; if blocker, block fails and action proceeds
            if (challengeOnBlock) {
                game.setState(GameState.APPLYING_ACTION);
            } else {
                game.setState(GameState.IN_PROGRESS);
            }
            game.logAction(new ActionRecord(
                    challenged.getId(),
                    ActionType.CHALLENGE,
                    challenger.getId(),
                    challenged.getId() + " loses challenge and reveals a card"
            ));

        }

        repo.save(game);
        return game;
    }

    public Game applyAction(Game game) {

        if (game.getState() != GameState.APPLYING_ACTION &&
                game.getState() != GameState.ACTION_DECLARED)
            throw new IllegalStateException("No action to apply");

        ActionType action = game.getDeclaredAction();
        Player actor = game.getPlayer(game.getActingPlayerId());
        Player target = game.getTargetPlayerId() != null
                ? game.getPlayer(game.getTargetPlayerId())
                : null;

//System Comment
        System.out.println("[ACTION TAKEN] " + actor.getId() + " performs " + action +
                (target != null ? " on " + target.getId() : ""));

        String log = actor.getId() + " performed " + action +
                (target != null ? " on " + target.getId() : "");

        for (Player p : game.getPlayers()) {
            p.getMemory().history.add(log);
        }


        // --- APPLY ACTION EFFECTS ---
        switch (action) {
            case INCOME -> {
                actor.addCoins(1);
                game.logAction(new ActionRecord(
                        actor.getId(), action, null,
                        actor.getId() + " gains 1 coin (INCOME)"
                ));
            }

            case FOREIGN_AID -> {
                actor.addCoins(2);
                game.logAction(new ActionRecord(
                        actor.getId(), action, null,
                        actor.getId() + " gains 2 coins (FOREIGN AID)"
                ));
            }

            case TAX -> {
                actor.addCoins(3);
                game.logAction(new ActionRecord(
                        actor.getId(), action, null,
                        actor.getId() + " gains 3 coins (DUKE TAX)"
                ));
            }

            case STEAL -> {
                int stolen = Math.min(2, target.getCoins());
                target.removeCoins(stolen);
                actor.addCoins(stolen);

                game.logAction(new ActionRecord(
                        actor.getId(), action, target.getId(),
                        actor.getId() + " steals " + stolen + " coins from " + target.getId()
                ));
            }

            case ASSASSINATE -> {
                actor.removeCoins(3);
                if (target != null) target.removeAnyCard();

                game.logAction(new ActionRecord(
                        actor.getId(), action, target.getId(),
                        actor.getId() + " assassinates " + target.getId()
                ));
            }

            case COUP -> {
                actor.removeCoins(7);
                if (target != null) target.removeAnyCard();

                game.logAction(new ActionRecord(
                        actor.getId(), action, target.getId(),
                        actor.getId() + " coups " + target.getId()
                ));
            }

            case EXCHANGE -> {
                // Minimal version: draw 2 cards and keep both
                Card c1 = game.drawCard();
                Card c2 = game.drawCard();
                actor.addCard(c1);
                actor.addCard(c2);

                game.logAction(new ActionRecord(
                        actor.getId(), action, null,
                        actor.getId() + " exchanges cards"
                ));
            }
        }

        // --- NEXT TURN ---
        game.nextTurn();

        if (game.getState() != GameState.FINISHED) {
            game.setState(GameState.IN_PROGRESS);
        }

        return saveIfFinished(game);
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