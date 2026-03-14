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

    public Game joinGame(String gameId, String playerId, String name) {
        Game game = repo.find(gameId);
        game.addPlayer(new Player(playerId, name));
        repo.save(game);
        return game;
    }

    public Game startGame(String gameId) {
        Game game = repo.find(gameId);
        game.startGame();
        repo.save(game);
        return game;
    }

    public Game declareAction(String gameId, String playerId, ActionType action, String targetId) {
        Game game = repo.find(gameId);
        game.declareAction(playerId, action, targetId);
        repo.save(game);
        return game;
    }

    public Game declareBlock(String gameId, String blockingPlayerId, CardType claimedRole) {
        Game game = repo.find(gameId);
        if (game.getState() != GameState.ACTION_DECLARED)
            throw new IllegalStateException("No action to block");
        game.setState(GameState.BLOCK_DECLARED);
        game.setBlockingPlayerId(blockingPlayerId);
        game.setBlockingRole(claimedRole);
        repo.save(game);
        return game;
    }

    public Game declareChallenge(String gameId, String challengerId) {
        Game game = repo.find(gameId);
        if (game.getState() != GameState.ACTION_DECLARED &&
                game.getState() != GameState.BLOCK_DECLARED)
            throw new IllegalStateException("Nothing to challenge");
        game.setState(GameState.CHALLENGE_PENDING);
        game.setChallengerId(challengerId);
        repo.save(game);
        return game;
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

        switch (action) {
            case INCOME -> actor.addCoins(1);
            case FOREIGN_AID -> actor.addCoins(2);
            case TAX -> actor.addCoins(3);
            case STEAL -> {
                int stolen = Math.min(2, target.getCoins());
                target.removeCoins(stolen);
                actor.addCoins(stolen);
            }
            case ASSASSINATE -> {
                actor.removeCoins(3);
                if (target != null) target.revealAny();
            }
            case COUP -> {
                actor.removeCoins(7);
                if (target != null) target.revealAny();
            }
            case EXCHANGE -> {
                // Minimal: draw 2 and keep them all; real game would let player choose
                actor.addCard(game.drawCard());
                actor.addCard(game.drawCard());
            }
        }

        game.nextTurn();
        if (game.getState() != GameState.FINISHED) {
            game.setState(GameState.IN_PROGRESS);
        }

        repo.save(game);
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