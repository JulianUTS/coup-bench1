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
        return repo.save(game);
    }

    public Game getGame(String gameId) {
        return repo.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    public Game joinGame(String gameId, String playerId, String name, String provider) {
        Game game = getGame(gameId);
        game.addPlayer(new Player(playerId, name, provider));
        return repo.save(game);
    }

    public Game startGame(String gameId) {
        Game game = getGame(gameId);
        game.startGame();
        return repo.save(game);
    }

    public Game declareAction(String gameId, String playerId, ActionType action, String targetId) {
        Game game = getGame(gameId);
        game.declareAction(playerId, action, targetId);
        return repo.save(game);
    }

    public Game declareBlock(String gameId, String blockingPlayerId, CardType claimedRole) {
        Game game = getGame(gameId);
        if (game.getState() != GameState.ACTION_DECLARED)
            throw new IllegalStateException("No action to block");

        game.setState(GameState.BLOCK_DECLARED);
        game.setBlockingPlayerId(blockingPlayerId);
        game.setBlockingRole(claimedRole);

        return repo.save(game);
    }

    public Game declareChallenge(String gameId, String challengerId) {
        Game game = getGame(gameId);
        if (game.getState() != GameState.ACTION_DECLARED &&
                game.getState() != GameState.BLOCK_DECLARED)
            throw new IllegalStateException("Nothing to challenge");

        game.setState(GameState.CHALLENGE_PENDING);
        game.setChallengerId(challengerId);

        return repo.save(game);
    }

    public Game resolveChallenge(String gameId) {
        Game game = getGame(gameId);

        boolean challengeOnBlock =
                game.getState() == GameState.BLOCK_DECLARED ||
                        (game.getState() == GameState.CHALLENGE_PENDING && game.getBlockingPlayerId() != null);

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
            challenger.revealAny();
            challenged.getCards().stream()
                    .filter(c -> c.getType() == claimedRole && !c.isRevealed())
                    .findFirst()
                    .ifPresent(card -> {
                        card.reveal();
                        game.discard(card);
                        challenged.addCard(game.drawCard());
                    });

            game.setState(challengeOnBlock ? GameState.IN_PROGRESS : GameState.APPLYING_ACTION);

        } else {
            challenged.revealAny();
            game.setState(challengeOnBlock ? GameState.APPLYING_ACTION : GameState.IN_PROGRESS);
        }

        return repo.save(game);
    }

    public Game applyAction(String gameId) {
        Game game = getGame(gameId);

        if (game.getState() != GameState.APPLYING_ACTION &&
                game.getState() != GameState.ACTION_DECLARED)
            throw new IllegalStateException("No action to apply");

        ActionType action = game.getDeclaredAction();
        Player actor = game.getPlayer(game.getActingPlayerId());
        Player target = game.getTargetPlayerId() != null
                ? game.getPlayer(game.getTargetPlayerId())
                : null;

        switch (action) {
            case INCOME -> {
                actor.addCoins(1);
                game.logAction(new ActionRecord(actor.getId(), action, null,
                        actor.getName() + " gains 1 coin (INCOME)"));
            }
            case FOREIGN_AID -> {
                actor.addCoins(2);
                game.logAction(new ActionRecord(actor.getId(), action, null,
                        actor.getName() + " gains 2 coins (FOREIGN AID)"));
            }
            case TAX -> {
                actor.addCoins(3);
                game.logAction(new ActionRecord(actor.getId(), action, null,
                        actor.getName() + " gains 3 coins (DUKE TAX)"));
            }
            case STEAL -> {
                int stolen = Math.min(2, target.getCoins());
                target.removeCoins(stolen);
                actor.addCoins(stolen);
                game.logAction(new ActionRecord(actor.getId(), action, target.getId(),
                        actor.getName() + " steals " + stolen + " coins from " + target.getName()));
            }
            case ASSASSINATE -> {
                actor.removeCoins(3);
                if (target != null) target.revealAny();
                game.logAction(new ActionRecord(actor.getId(), action, target.getId(),
                        actor.getName() + " assassinates " + target.getName()));
            }
            case COUP -> {
                actor.removeCoins(7);
                if (target != null) target.revealAny();
                game.logAction(new ActionRecord(actor.getId(), action, target.getId(),
                        actor.getName() + " coups " + target.getName()));
            }
            case EXCHANGE -> {
                Card c1 = game.drawCard();
                Card c2 = game.drawCard();
                actor.addCard(c1);
                actor.addCard(c2);
                game.logAction(new ActionRecord(actor.getId(), action, null,
                        actor.getName() + " exchanges cards"));
            }
        }

        game.nextTurn();
        if (game.getState() != GameState.FINISHED) {
            game.setState(GameState.IN_PROGRESS);
        }

        return repo.save(game);
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
