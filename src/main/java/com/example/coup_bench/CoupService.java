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
        repo.save(game);
        return game;
    }

    public Game getGame(String gameId) {
        return repo.find(gameId);
    }

    public Game joinGame(String gameId, String playerId, String name, String provider) {
        Game game = repo.find(gameId);
        game.addPlayer(new Player(playerId, provider));
        repo.save(game);
        return game;
    }

    public Game startGame(String gameId) {
        Game game = repo.find(gameId);
        game.startGame();
        repo.save(game);
        return game;
    }

    private Game saveIfFinished(Game game) {
        if (game.getState() == GameState.FINISHED) {
            repo.save(game);
        }
        return game;
    }

    public Game declareAction(String gameId, String playerId, ActionType action, String targetId) {
        Game game = repo.find(gameId);
        Player actor = game.getPlayer(playerId);

        // --- STRICT RULES ---

        // 1. Forced COUP at 10+ coins
        if (actor.getCoins() >= 10) {
            action = ActionType.COUP;
        }

        // 2. Validate action legality
        switch (action) {
            case ASSASSINATE -> {
                if (actor.getCoins() < 3)
                    throw new IllegalStateException("Not enough coins to ASSASSINATE");
            }
            case COUP -> {
                if (actor.getCoins() < 7)
                    throw new IllegalStateException("Not enough coins to COUP");
                if (targetId == null)
                    throw new IllegalStateException("COUP requires a target");
            }
            case STEAL -> {
                if (targetId == null)
                    throw new IllegalStateException("STEAL requires a target");
            }
        }

        game.declareAction(playerId, action, targetId);
        return saveIfFinished(game);
    }

    public Game declareBlock(String gameId, String blockingPlayerId, CardType claimedRole) {
        Game game = repo.find(gameId);

        // Cannot block INCOME
        if (game.getDeclaredAction() == ActionType.INCOME)
            throw new IllegalStateException("Cannot block INCOME");

        // Cannot block COUP
        if (game.getDeclaredAction() == ActionType.COUP)
            throw new IllegalStateException("Cannot block COUP");

        game.setState(GameState.BLOCK_DECLARED);
        game.setBlockingPlayerId(blockingPlayerId);
        game.setBlockingRole(claimedRole);

        return saveIfFinished(game);
    }


    public Game declareChallenge(String gameId, String challengerId) {
        Game game = repo.find(gameId);

        // Cannot challenge INCOME
        if (game.getDeclaredAction() == ActionType.INCOME)
            throw new IllegalStateException("Cannot challenge INCOME");

        game.setState(GameState.CHALLENGE_PENDING);
        game.setChallengerId(challengerId);

        return saveIfFinished(game);
    }


    public Game resolveChallenge(String gameId) {
        Game game = repo.find(gameId);

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
            challenger.revealAny();
            // Challenged swaps that role
            challenged.getCards().stream()
                    .filter(c -> c.getType() == claimedRole && !c.isRevealed())
                    .findFirst()
                    .ifPresent(card -> {
                        card.reveal();
                        game.discard(card);
                        challenged.addCard(game.drawCard());
                    });
            // If challenge was on block, action is blocked; otherwise action proceeds
            game.setState(challengeOnBlock ? GameState.IN_PROGRESS : GameState.APPLYING_ACTION);
        } else {
            // Challenged loses
            challenged.revealAny();
            // If challenged was actor, action fails; if blocker, block fails and action proceeds
            if (challengeOnBlock) {
                game.setState(GameState.APPLYING_ACTION);
            } else {
                game.setState(GameState.IN_PROGRESS);
            }
        }

        repo.save(game);
        return game;
    }

    public Game applyAction(String gameId) {
        Game game = repo.find(gameId);

        if (game.getState() != GameState.APPLYING_ACTION &&
                game.getState() != GameState.ACTION_DECLARED)
            throw new IllegalStateException("No action to apply");

        ActionType action = game.getDeclaredAction();
        Player actor = game.getPlayer(game.getActingPlayerId());
        Player target = game.getTargetPlayerId() != null
                ? game.getPlayer(game.getTargetPlayerId())
                : null;

        System.out.println("[ACTION TAKEN] " + actor.getId() + " performs " + action +
                (target != null ? " on " + target.getId() : ""));

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
                if (target != null) target.revealAny();

                game.logAction(new ActionRecord(
                        actor.getId(), action, target.getId(),
                        actor.getId() + " assassinates " + target.getId()
                ));
            }

            case COUP -> {
                actor.removeCoins(7);
                if (target != null) target.revealAny();

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