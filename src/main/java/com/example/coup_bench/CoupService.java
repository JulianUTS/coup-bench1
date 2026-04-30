package com.example.coup_bench;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.repoModels.*;
import com.example.coup_bench.repo.GameRepository;
import com.example.coup_bench.repo.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CoupService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;

    public CoupService(GameRepository repo, PlayerRepository playerRepo) {
        this.gameRepo = repo;
        this.playerRepo = playerRepo;
    }

    public Game createGame() {
        return new Game(UUID.randomUUID().toString());
    }

    public Game joinGame(Game game, String playerId, String personality) {
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

        summary.setTotalGameDurationSec(diffSeconds(game.getTimestampStart()));


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
        summary.setBluffLog(game.getBluffLog());
        summary.setInteractions(game.getInteractionLog());
        summary.setTurnSnapshots(game.getTurnSnapshotLog());
        summary.setSeed(game.getSeed());
        summary.setSeatOrder(game.getSeatOrder());

        return summary;
    }
    public AgentLifetimeStats getAgentLifetimeStats(Player player, PlayerRepository playerRepo, GameSummary gameSummary) {
        String provider = player.getId();
        String personality = player.getPersonality();

        // Load or create provider-level stats
        AgentLifetimeStats stats =
                playerRepo.findById(provider).orElse(new AgentLifetimeStats(provider));

        // -------------------------
        // 1. Provider-level updates
        // -------------------------

        stats.setTotalGames(stats.getTotalGames() + 1);

        if (player.isAlive()) stats.setWins(stats.getWins() + 1);
        else stats.setLosses(stats.getLosses() + 1);

        // Aggression
        stats.setTotalStealAttempts(stats.getTotalStealAttempts() + player.getStealAttempts());
        stats.setTotalAssassinationAttempts(stats.getTotalAssassinationAttempts() + player.getAssassinationAttempts());
        stats.setTotalCoupsPerformed(stats.getTotalCoupsPerformed() + player.getCoupsPerformed());

        // Risk
        stats.setTotalBluffsAttempted(stats.getTotalBluffsAttempted() + player.getBluffsAttempted());
        stats.setTotalChallengesIssued(stats.getTotalChallengesIssued() + player.getChallengesIssued());

        // Defense
        stats.setTotalBlocksIssued(stats.getTotalBlocksIssued() + player.getBlocksIssued());

        // Survival
        stats.setTotalTurnsSurvived(stats.getTotalTurnsSurvived() + player.getTurnsSurvived());
        stats.setTotalTurnsPlayed(stats.getTotalTurnsPlayed() + gameSummary.getTotalTurns());

        // Game duration
        stats.setTotalGameDurationMs(stats.getTotalGameDurationSec() + gameSummary.getTotalGameDurationSec());

        // Interaction heatmaps
        if (player.getLastTargetProvider() != null) {
            stats.getTargetedProviders().merge(player.getLastTargetProvider(), 1, Integer::sum);
        }

        if (player.getLastChallengedProvider() != null) {
            stats.getChallengedProviders().merge(player.getLastChallengedProvider(), 1, Integer::sum);
        }

        // Recompute provider averages
        stats.setAverageSurvivalRate(
                safeRate(stats.getTotalTurnsSurvived(), stats.getTotalTurnsPlayed())
        );

        stats.setAverageGameDurationMs(
                (double) stats.getTotalGameDurationSec() / stats.getTotalGames()
        );

        // -------------------------
        // 2. Personality-level updates
        // -------------------------

        PersonalityStats ps = stats.getPersonalities()
                .computeIfAbsent(personality, k -> new PersonalityStats());

        ps.setTotalGames(ps.getTotalGames() + 1);

        if (player.isAlive()) ps.setWins(ps.getWins() + 1);
        else ps.setLosses(ps.getLosses() + 1);

        // Raw stats
        ps.setBluffsAttempted(ps.getBluffsAttempted() + player.getBluffsAttempted());
        ps.setBluffsSuccessful(ps.getBluffsSuccessful() + player.getBluffsSuccessful());
        ps.setBluffsFailed(ps.getBluffsFailed() + player.getBluffsFailed());

        ps.setChallengesIssued(ps.getChallengesIssued() + player.getChallengesIssued());
        ps.setChallengesWon(ps.getChallengesWon() + player.getChallengesWon());
        ps.setChallengesLost(ps.getChallengesLost() + player.getChallengesLost());

        ps.setBlocksIssued(ps.getBlocksIssued() + player.getBlocksIssued());
        ps.setBlocksSuccessful(ps.getBlocksSuccessful() + player.getBlocksSuccessful());
        ps.setBlocksFailed(ps.getBlocksFailed() + player.getBlocksFailed());

        ps.setIncomeCount(ps.getIncomeCount() + player.getIncomeCount());
        ps.setTaxCount(ps.getTaxCount() + player.getTaxCount());

        ps.setStealAttempts(ps.getStealAttempts() + player.getStealAttempts());
        ps.setStealSuccesses(ps.getStealSuccesses() + player.getStealSuccesses());

        ps.setAssassinationAttempts(ps.getAssassinationAttempts() + player.getAssassinationAttempts());
        ps.setAssassinationSuccesses(ps.getAssassinationSuccesses() + player.getAssassinationSuccesses());

        ps.setCoupsPerformed(ps.getCoupsPerformed() + player.getCoupsPerformed());

        // Survival
        ps.setTotalTurnsSurvived(ps.getTotalTurnsSurvived() + player.getTurnsSurvived());
        ps.setAverageTurnsSurvived(
                (double) ps.getTotalTurnsSurvived() / ps.getTotalGames()
        );

        // Derived analytics
        ps.setAggressionScore(
                ps.getStealAttempts()
                        + ps.getAssassinationAttempts()
                        + ps.getCoupsPerformed()
        );

        ps.setRiskScore(
                ps.getBluffsAttempted()
                        + ps.getChallengesIssued()
        );

        ps.setBluffSuccessRate(safeRate(ps.getBluffsSuccessful(), ps.getBluffsAttempted()));
        ps.setChallengeSuccessRate(safeRate(ps.getChallengesWon(), ps.getChallengesIssued()));
        ps.setBlockSuccessRate(safeRate(ps.getBlocksSuccessful(), ps.getBlocksIssued()));
        ps.setStealSuccessRate(safeRate(ps.getStealSuccesses(), ps.getStealAttempts()));
        ps.setAssassinationSuccessRate(safeRate(ps.getAssassinationSuccesses(), ps.getAssassinationAttempts()));

        // Optional: compute entropy (unpredictability)
        ps.setActionEntropy(calculateEntropy(ps));

        return stats;
    }
    private double safeRate(int success, int attempts) {
        return attempts == 0 ? 0.0 : (double) success / attempts;
    }

    private double calculateEntropy(PersonalityStats ps) {

        // Collect all action counts
        int[] counts = {
                ps.getIncomeCount(),
                ps.getTaxCount(),
                ps.getStealAttempts(),
                ps.getAssassinationAttempts(),
                ps.getCoupsPerformed(),
                ps.getBluffsAttempted(),
                ps.getChallengesIssued(),
                ps.getBlocksIssued()
        };

        int total = 0;
        for (int c : counts) total += c;

        if (total == 0) return 0.0; // no actions = no entropy

        double entropy = 0.0;

        for (int c : counts) {
            if (c == 0) continue; // skip zero-probability actions

            double p = (double) c / total;
            entropy += -p * (Math.log(p) / Math.log(2)); // log base 2
        }

        return entropy;
    }

    public static long diffSeconds(long startMs) {
        long nowMs = System.currentTimeMillis();
        return (nowMs - startMs) / 1000;
    }


    public Game saveIfFinished(Game game) {
        if (game.getState() == GameState.FINISHED || game.getState() == GameState.INVALID) {
            GameSummary gamesummary = getGameSummary(game);
            gameRepo.save(getGameSummary(game));
            for(Player p : game.getPlayers()){
                playerRepo.save(getAgentLifetimeStats(p, playerRepo, gamesummary));
            }
        }
        return game;
    }


    public Game invalidGame(Game game) {
        game.logGameMemory("3 Invalid Actions used in a row, game is invalid");
        game.setState(GameState.INVALID);
        return saveIfFinished(game);
    }

    private Game invalidateAction(Game game, ActionRecord actionRecord, String message) {
        game.logGameMemory(actionRecord.getPlayerId() + " calls invalid " + actionRecord.getAction() + ": " + message);
        InvalidActionRecord invalidActionRecord = new InvalidActionRecord(
                actionRecord.getPlayerId(),
                actionRecord.getAction(),
                actionRecord.getTargetId(),
                actionRecord.getDescription(),
                message
        );
        game.logInvalidAction(invalidActionRecord);
        game.incrementInvalidAction();
        return game;
    }

    private String validateAction(Game game, ActionRecord actionRecord) {

        Player player = game.getPlayer(actionRecord.getPlayerId());
        ActionType action = actionRecord.getAction();
        String targetId = actionRecord.getTargetId();

        if (player.getCoins() >= 10 && action != ActionType.COUP)
            return ("Must choose COUP if 10 coins or more");

        switch (action) {
            case ASSASSINATE -> {
                if (player.getCoins() < 3)
                    return ("Not enough coins to ASSASSINATE");
                if (targetId == null)
                    return ("ASSASSINATE requires a target");
                if (targetId.equals(player.getId()))
                    return ("Cannot ASSASSINATE yourself");
                if (!game.getPlayer(targetId).isAlive())
                    return ("ASSASSINATE requires an alive target");
            }
            case COUP -> {
                if (player.getCoins() < 7)
                    return "Not enough coins to COUP";
                if (targetId == null)
                    return "COUP requires a target";
                if (targetId.equals(player.getId()))
                    return "Cannot COUP yourself";
                if (!game.getPlayer(targetId).isAlive())
                    return "COUP requires an alive target";
            }
            case STEAL -> {
                if (targetId == null)
                    return "STEAL requires a target";
                if (targetId.equals(player.getId()))
                    return "Cannot STEAL from yourself";
                if (!game.getPlayer(targetId).isAlive())
                    return "STEAL requires an alive target";
                if (game.getPlayer(targetId).getCoins() == 0)
                    return "STEAL requires a target with more than 0 coins";
            }
            case TAX, FOREIGN_AID, INCOME, EXCHANGE -> {
                if (targetId != null)
                    return action + " must not have a target";
            }
        }
        return null;
    }

    public Game declareAction(Game game, ActionRecord actionRecord) {

        Player player = game.getPlayer(actionRecord.getPlayerId());
        ActionType action = actionRecord.getAction();

        // Validate first
        String invalidResult = validateAction(game, actionRecord);
        if (invalidResult != null)
            return invalidateAction(game, actionRecord, invalidResult);

        if (!player.hasCard(roleForAction(action))) {
            player.incrementBluffsAttempted();
            game.logBluff(actionRecord);
        }

        if(actionRecord.getTargetId() != null){
            player.setLastTargetProvider(actionRecord.getTargetId());
        }

        game.resetInvalidAction();
        game.declareAction(actionRecord);
        return game;
    }

    public Game logAction(Game game, ActionRecord actionRecord) {
        game.logAction(actionRecord);
         return game;
    }

    public Game declareBlock(Game game, String blockerId, AiReaction aiReaction) {

        game.setState(GameState.BLOCK_DECLARED);
        game.setBlockerId(blockerId);
        game.setBlockingRole(roleForAction(aiReaction.action));
        game.logGameMemory(blockerId + " declares " + aiReaction.action + " on " + game.getActingPlayerId());
        game.incrementTotalBlocks();

        ActionRecord record = new ActionRecord(
                blockerId,
                aiReaction.action,
                game.getActingPlayerId(),
                aiReaction.reason);

        game.getPlayer(blockerId).incrementBlocksIssued();
        if (!game.getPlayer(blockerId).hasCard(game.getBlockingRole())) {
            game.getPlayer(blockerId).incrementBluffsAttempted();
            game.logBluff(record);
        }

        game.logAction(record);

        return game;
    }

    public Game declareChallenge(Game game, String challengerId, AiReaction aiReaction) {

        game.setState(GameState.CHALLENGE_DECLARED);
        game.setChallengerId(challengerId);
        String targetId = game.getBlockerId() != null ? game.getBlockerId() : game.getActingPlayerId();
        game.logGameMemory(challengerId + " declares " + aiReaction.action + " on " + targetId);
        game.incrementTotalChallenges();

        game.getPlayer(challengerId).incrementChallengesIssued();
        game.getPlayer(challengerId).setLastChallengedProvider(targetId);

        game.logAction(new ActionRecord(
                challengerId,
                ActionType.CHALLENGE,
                targetId,
                aiReaction.reason
        ));

        return game;
    }

    public CardType chooseCard(Game game, Player player, AiDecisionService ai) {
        if (player.getCards().size() == 1) {
            return player.getCards().getFirst();
        }
        return ai.getCardToLoose(game, player);

    }


    public Game resolveChallenge(Game game, AiDecisionService ai) {

        Player challenger = game.getPlayer(game.getChallengerId());
        Player claimedPlayer;
        CardType claimedRole;
        boolean isBlockChallenge;

        if (game.getBlockerId() != null) {
            isBlockChallenge = true;
            claimedPlayer = game.getPlayer(game.getBlockerId());
            claimedRole = game.getBlockingRole();
        } else {
            isBlockChallenge = false;
            claimedPlayer = game.getPlayer(game.getActingPlayerId());
            claimedRole = roleForAction(game.getDeclaredAction());
        }

        boolean claimIsTrue = claimedPlayer.hasCard(claimedRole);

        if (claimIsTrue) {
            handleTrueClaim(game, challenger, claimedPlayer, claimedRole, isBlockChallenge, ai);
        } else {
            handleFalseClaim(game, challenger, claimedPlayer, isBlockChallenge, ai);
        }

        if (game.getPlayers().stream().filter(Player::isAlive).count() <= 1) {
            game.setState(GameState.FINISHED);
        }
        return game;
    }

    private void handleTrueClaim(Game game, Player challenger, Player claimedPlayer,
                                 CardType claimedRole, boolean isBlockChallenge,
                                 AiDecisionService ai) {

        game.logGameMemory(claimedPlayer.getId() + " wins challenge");

        challenger.incrementChallengesLost();
        //Unsuccessful challenge
        game.logInteraction(new InteractionRecord(challenger.getId(), claimedPlayer.getId(),
                ActionType.CHALLENGE, false));

        CardType lostCard = chooseCard(game, challenger, ai);

        game.switchCard(claimedPlayer.getId(), claimedRole);
        game.removeCard(challenger.getId(), lostCard);

        if (isBlockChallenge) {
            if(!game.getPlayer(game.getActingPlayerId()).hasCard(roleForAction(game.getDeclaredAction()))){
                game.getPlayer(game.getActingPlayerId()).incrementBluffsFailed();
            }
            if(game.getTargetId() != null) {
                game.logInteraction(new InteractionRecord(game.getActingPlayerId(),
                        game.getTargetId(), game.getDeclaredAction(), false));

            }
            game.setState(GameState.BLOCK_DECLARED);
        } else {
            game.setState(GameState.APPLYING_ACTION);
        }
    }

    private void handleFalseClaim(Game game, Player challenger, Player claimedPlayer,
                                  boolean isBlockChallenge, AiDecisionService ai) {

        game.logGameMemory(challenger.getId() + " wins challenge");

        claimedPlayer.incrementBluffsFailed();
        challenger.incrementChallengesWon();
        //Successful challenge
        game.logInteraction(new InteractionRecord(game.getChallengerId(), game.getBlockerId(),
                ActionType.CHALLENGE, true));

        CardType lostCard = chooseCard(game, claimedPlayer, ai);
        game.removeCard(claimedPlayer.getId(), lostCard);

        if (isBlockChallenge) {
            claimedPlayer.incrementBlocksFailed();
            //Unsuccessful block
            game.logInteraction(new InteractionRecord(game.getBlockerId(), game.getActingPlayerId(),
                    actionForRole(game.getBlockingRole()), false));

            game.setState(GameState.APPLYING_ACTION);
        } else {
            if(game.getTargetId() != null) {
                game.logInteraction(new InteractionRecord(game.getActingPlayerId(),
                        game.getTargetId(), game.getDeclaredAction(), false));

            }
            game.setState(GameState.CHALLENGE_DECLARED);
        }
    }

    public Game applyAction(Game game, AiDecisionService ai) {

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
                player.incrementAssassinationSuccesses();
                game.logGameMemory(player.getId() + " assassinates " + target.getId());
                game.removeCard(target.getId(), chooseCard(game, target, ai));

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
        //Successful Targeted Action
        if(target != null){
            game.logInteraction(new InteractionRecord(player.getId(), target.getId(), action, true));
        }

        return game;
    }

    public Game nextTurn(Game game){
        game.clearChallengeData();
        TurnSnapshot snap = new TurnSnapshot(
                game.getTurn(),
                game.getPlayers().stream()
                        .collect(Collectors.toMap(Player::getId, Player::getCoins)),
                game.getPlayers().stream()
                        .collect(Collectors.toMap(Player::getId, p -> p.getCards().size())),
                game.getDeclaredAction(),
                game.getActingPlayerId(),
                game.getTargetId()
        );
        game.logTurnSnapshot(snap);



        game.nextTurn();
        if (game.getState() != GameState.FINISHED) {
            game.setState(GameState.IN_PROGRESS);
        }
        return saveIfFinished(game);
    };

    public Game applyBlock(Game game){
        game.getPlayer(game.getBlockerId()).incrementBlocksSuccessful();
        //Unsuccessful targeted action
        if(game.getTargetId() != null){
            game.logInteraction(new InteractionRecord(game.getActingPlayerId(), game.getTargetId(),
                    game.getDeclaredAction(), false));
        }
        //Successful Block
        game.logInteraction(new InteractionRecord(game.getBlockerId(), game.getActingPlayerId(),
               actionForRole(game.getBlockingRole()), true));


        if(!game.getPlayer(game.getBlockerId()).hasCard(game.getBlockingRole())){
            game.getPlayer(game.getBlockerId()).incrementBluffsSuccessful();
        }
        if(!game.getPlayer(game.getActingPlayerId()).hasCard(roleForAction(game.getDeclaredAction()))){
            game.getPlayer(game.getActingPlayerId()).incrementBluffsFailed();
        }
        return game;
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
    private ActionType actionForRole(CardType card) {
        return switch (card) {
            case AMBASSADOR -> ActionType.BLOCK_USING_AMBASSADOR ;
            case CAPTAIN -> ActionType.BLOCK_USING_CAPTAIN;
            case CONTESSA -> ActionType.BLOCK_USING_CONTESSA;
            case DUKE -> ActionType.BLOCK_USING_DUKE;

            default -> null;
        };
    }
}