package com.example.coup_bench.util;

import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.GameStats;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.model.PlayerStats;
import com.example.coup_bench.model.repoModels.AgentLifetimeStats;
import com.example.coup_bench.model.repoModels.GameSummary;
import com.example.coup_bench.model.repoModels.PersonalityStats;
import com.example.coup_bench.repo.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class RepoUtil {

    public static GameSummary getGameSummary(Game game) {
        GameStats gameStats = game.getGameStats();
        GameSummary summary = new GameSummary();

// Basic identifiers
        summary.setId(null); // MongoDB will generate this
        summary.setGameId(game.getId());
        summary.setTrial(game.getTrial());

// Timestamps

        summary.setTotalGameDurationSec(diffSeconds(game.getTimestampStart()));


// Game stats
        summary.setNumberOfPlayers(game.getPlayers().size());
        summary.setWinnerId(game.getWinnerId(game));
        summary.setTotalTurns(game.getTurn());
        summary.setTotalActions(game.getActionLog().size());
        summary.setTotalChallenges(gameStats.getTotalChallenges());
        summary.setTotalBlocks(gameStats.getTotalBlocks());
        summary.setTotalInvalidActions(game.getInvalidActionLog().size());

// Game memory (if you store AI thoughts or logs)
        summary.setGameMemory(new ArrayList<>(game.getGameMemory()));

// Invalid actions
        summary.setInvalidActions(new ArrayList<>(game.getInvalidActionLog()));

// Full action log
        summary.setActions(new ArrayList<>(game.getActionLog()));

// Final player states
        summary.setPlayers(game.getPlayers());
        summary.setBluffLog(gameStats.getBluffLog());
        summary.setInteractions(gameStats.getInteractionLog());
        summary.setTurnSnapshots(gameStats.getTurnSnapshotLog());
        summary.setSeed(game.getSeed());
        summary.setSeatOrder(game.getSeatOrder());
        summary.setChallengeLog(gameStats.getChallengeLog());
        summary.setCoinsPerTurn(gameStats.getCoinsPerTurn());
        summary.setBluffsPerTurn(gameStats.getBluffsPerTurn());

        return summary;
    }

    public static AgentLifetimeStats getAgentLifetimeStats(Player player, PlayerRepository playerRepo, GameSummary gameSummary) {
        String provider = player.getId();
        PlayerStats playerStats = player.getPlayerStats();
        String personality = player.getPersonality();

        // Load or create provider-level lifetimeStats
        AgentLifetimeStats lifetimeStats =
                playerRepo.findByProviderAndPlayerCount(provider, gameSummary.getNumberOfPlayers())
                        .orElse(new AgentLifetimeStats(provider, gameSummary.getNumberOfPlayers()));

        // -------------------------
        // 1. Provider-level updates
        // -------------------------
        // Core outcomes
        lifetimeStats.setTotalGames(lifetimeStats.getTotalGames() + 1);

        if (player.isAlive()) lifetimeStats.setWins(lifetimeStats.getWins() + 1);
        else lifetimeStats.setLosses(lifetimeStats.getLosses() + 1);



// Aggression – attempts
        lifetimeStats.setTotalIncomeCount(lifetimeStats.getTotalIncomeCount() + playerStats.getIncomeCount());
        lifetimeStats.setTotalTaxAttempts(lifetimeStats.getTotalTaxAttempts() + playerStats.getTaxAttempts());
        lifetimeStats.setTotalForeignAidAttempts(lifetimeStats.getTotalForeignAidAttempts() + playerStats.getForeignAidAttempts());
        lifetimeStats.setTotalExchangeAttempts(lifetimeStats.getTotalExchangeAttempts() + playerStats.getExchangeAttempts());
        lifetimeStats.setTotalStealAttempts(lifetimeStats.getTotalStealAttempts() + playerStats.getStealAttempts());
        lifetimeStats.setTotalAssassinationAttempts(lifetimeStats.getTotalAssassinationAttempts() + playerStats.getAssassinationAttempts());
        lifetimeStats.setTotalCoupCount(lifetimeStats.getTotalCoupCount() + playerStats.getCoupsCount());

// Aggression – successes
        lifetimeStats.setTotalTaxSuccessful(lifetimeStats.getTotalTaxSuccessful() + playerStats.getTaxSuccessful());
        lifetimeStats.setTotalForeignAidSuccessful(lifetimeStats.getTotalForeignAidSuccessful() + playerStats.getForeignAidSuccessful());
        lifetimeStats.setTotalExchangeSuccessful(lifetimeStats.getTotalExchangeSuccessful() + playerStats.getExchangeSuccessful());
        lifetimeStats.setTotalStealSuccesses(lifetimeStats.getTotalStealSuccesses() + playerStats.getStealSuccesses());
        lifetimeStats.setTotalAssassinationSuccesses(lifetimeStats.getTotalAssassinationSuccesses() + playerStats.getAssassinationSuccesses());

// --- AGGRESSION FAILURES (attempts - successes) ---
        lifetimeStats.setTotalTaxFailed(
                lifetimeStats.getTotalTaxFailed()
                        + (playerStats.getTaxAttempts() - playerStats.getTaxSuccessful())
        );

        lifetimeStats.setTotalForeignAidBlocked(
                lifetimeStats.getTotalForeignAidBlocked()
                        + (playerStats.getForeignAidAttempts() - playerStats.getForeignAidSuccessful())
        );

        lifetimeStats.setTotalExchangeFailed(
                lifetimeStats.getTotalExchangeFailed()
                        + (playerStats.getExchangeAttempts() - playerStats.getExchangeSuccessful())
        );

        lifetimeStats.setTotalStealFailed(
                lifetimeStats.getTotalStealFailed()
                        + (playerStats.getStealAttempts() - playerStats.getStealSuccesses())
        );

        lifetimeStats.setTotalAssassinationFailed(
                lifetimeStats.getTotalAssassinationFailed()
                        + (playerStats.getAssassinationAttempts() - playerStats.getAssassinationSuccesses())
        );


// Risk
        lifetimeStats.setTotalBluffsAttempted(lifetimeStats.getTotalBluffsAttempted() + playerStats.getBluffsAttempted());
        lifetimeStats.setTotalBluffsSuccessful(lifetimeStats.getTotalBluffsSuccessful() + playerStats.getBluffsSuccessful());
        lifetimeStats.setTotalBluffsFailed(lifetimeStats.getTotalBluffsFailed() + playerStats.getBluffsFailed());
        lifetimeStats.setTotalChallengesIssued(lifetimeStats.getTotalChallengesIssued() + playerStats.getChallengesIssued());
        lifetimeStats.setTotalChallengesWon(lifetimeStats.getTotalChallengesWon() + playerStats.getChallengesWon());
        lifetimeStats.setTotalChallengesLost(lifetimeStats.getTotalChallengesLost() + playerStats.getChallengesLost());

// Defense
        lifetimeStats.setTotalBlocksIssued(lifetimeStats.getTotalBlocksIssued() + playerStats.getBlocksIssued());
        lifetimeStats.setTotalBlocksSuccessful(lifetimeStats.getTotalBlocksSuccessful() + playerStats.getBlocksSuccessful());
        lifetimeStats.setTotalBlocksFailed(lifetimeStats.getTotalBlocksFailed() + playerStats.getBlocksFailed());
        lifetimeStats.setTotalTimesBlocked(lifetimeStats.getTotalTimesBlocked() + playerStats.getTimesBlocked());

        // Survival
        lifetimeStats.setTotalTurnsSurvived(lifetimeStats.getTotalTurnsSurvived() + playerStats.getTurnsSurvived());
        lifetimeStats.setTotalTurnsPlayed(lifetimeStats.getTotalTurnsPlayed() + gameSummary.getTotalTurns());
        lifetimeStats.setAverageSurvivalRate(
                safeRate(lifetimeStats.getTotalTurnsSurvived(), lifetimeStats.getTotalTurnsPlayed())
        );

        lifetimeStats.setTotalCoinGained(lifetimeStats.getTotalCoinGained()+playerStats.getTotalCoinGained());
        lifetimeStats.setTotalCoinsSpent(lifetimeStats.getTotalCoinsSpent()+playerStats.getTotalCoinsSpent());

        // Game duration
        lifetimeStats.setTotalGameDurationSec(lifetimeStats.getTotalGameDurationSec() + gameSummary.getTotalGameDurationSec());
        lifetimeStats.setAverageGameDurationSec(
                (double) lifetimeStats.getTotalGameDurationSec() / lifetimeStats.getTotalGames()
        );

        // Interaction heatmaps
        if (!playerStats.getActionTargets().isEmpty()) {
            playerStats.getActionTargets().forEach((key, value) ->
                    lifetimeStats.getActionTargets().merge(key, value, Integer::sum)
            );
        }

        if (!playerStats.getBlockTargets().isEmpty()) {
            playerStats.getBlockTargets().forEach((key, value) ->
                    lifetimeStats.getBlockTargets().merge(key, value, Integer::sum)
            );
        }
        if (!playerStats.getChallengeTargets().isEmpty()) {
            playerStats.getChallengeTargets().forEach((key, value) ->
                    lifetimeStats.getChallengeTargets().merge(key, value, Integer::sum)
            );
        }
        if(playerStats.getKilledBy() != null) {
            lifetimeStats.getKilledBy().merge(playerStats.getKilledBy(), 1, Integer::sum);
        }
        if(playerStats.getCauseOfDeath() != null) {
            lifetimeStats.getCauseOfDeath().merge(playerStats.getCauseOfDeath().toString(), 1, Integer::sum);
        }
        if(!playerStats.getPlayersKilled().isEmpty()) {
            for (String killedPlayerId : playerStats.getPlayersKilled()) {
                lifetimeStats.getPlayersKilled().merge(killedPlayerId, 1, Integer::sum);
            }
        }

        int seatIndex = gameSummary.getSeatOrder().get(provider);
        if (player.isAlive())
                lifetimeStats.getWinsFromSeatIndex().merge(seatIndex, 1, Integer::sum);
        else lifetimeStats.getLossesFromSeatIndex().merge(seatIndex, 1, Integer::sum);

        // Recompute provider averages

        // -------------------------
        // 2. Personality-level updates
        // -------------------------

        PersonalityStats ps = lifetimeStats.getPersonalities()
                .computeIfAbsent(personality, k -> new PersonalityStats());

        ps.setTotalGames(ps.getTotalGames() + 1);

        if (player.isAlive()) ps.setWins(ps.getWins() + 1);
        else ps.setLosses(ps.getLosses() + 1);

        // Raw lifetimeStats
        ps.setBluffsAttempted(ps.getBluffsAttempted() + playerStats.getBluffsAttempted());
        ps.setBluffsSuccessful(ps.getBluffsSuccessful() + playerStats.getBluffsSuccessful());
        ps.setBluffsFailed(ps.getBluffsFailed() + playerStats.getBluffsFailed());

        ps.setChallengesIssued(ps.getChallengesIssued() + playerStats.getChallengesIssued());
        ps.setChallengesWon(ps.getChallengesWon() + playerStats.getChallengesWon());
        ps.setChallengesLost(ps.getChallengesLost() + playerStats.getChallengesLost());

        ps.setBlocksIssued(ps.getBlocksIssued() + playerStats.getBlocksIssued());
        ps.setBlocksSuccessful(ps.getBlocksSuccessful() + playerStats.getBlocksSuccessful());
        ps.setBlocksFailed(ps.getBlocksFailed() + playerStats.getBlocksFailed());
        ps.setTimesBlocked(ps.getTimesBlocked() + playerStats.getTimesBlocked());

        ps.setIncomeCount(ps.getIncomeCount() + playerStats.getIncomeCount());
        ps.setTaxAttempts(ps.getTaxAttempts() + playerStats.getTaxAttempts());
        ps.setTaxSuccessful(ps.getTaxSuccessful() + playerStats.getTaxSuccessful());
        ps.setExchangeAttempts(ps.getExchangeAttempts() + playerStats.getExchangeAttempts());
        ps.setExchangeSuccessful(ps.getExchangeSuccessful() + playerStats.getExchangeSuccessful());
        ps.setForeignAidAttempts(ps.getForeignAidAttempts() + playerStats.getForeignAidAttempts());
        ps.setForeignAidSuccessful(ps.getForeignAidSuccessful() + playerStats.getForeignAidSuccessful());


        ps.setStealAttempts(ps.getStealAttempts() + playerStats.getStealAttempts());
        ps.setStealSuccesses(ps.getStealSuccesses() + playerStats.getStealSuccesses());

        ps.setAssassinationAttempts(ps.getAssassinationAttempts() + playerStats.getAssassinationAttempts());
        ps.setAssassinationSuccesses(ps.getAssassinationSuccesses() + playerStats.getAssassinationSuccesses());

        ps.setCoupsPerformed(ps.getCoupsPerformed() + playerStats.getCoupsCount());

        ps.setTotalCoinGained(ps.getTotalCoinGained() + playerStats.getTotalCoinGained());
        ps.setTotalCoinsSpent(ps.getTotalCoinsSpent() + playerStats.getTotalCoinsSpent());
        ps.setNetCoinFlow(ps.getTotalCoinGained()-ps.getTotalCoinsSpent());

        if(playerStats.getCauseOfDeath() != null) {
            ps.getCauseOfDeath().merge(playerStats.getCauseOfDeath().toString(), 1, Integer::sum);
        }

        if (player.isAlive())
            ps.getWinsFromSeatIndex().merge(seatIndex, 1, Integer::sum);
        else ps.getLossesFromSeatIndex().merge(seatIndex, 1, Integer::sum);

        if (!playerStats.getActionTargets().isEmpty()) {
            playerStats.getActionTargets().forEach((key, value) ->
                    ps.getActionTargets().merge(key, value, Integer::sum)
            );
        }

        if (!playerStats.getBlockTargets().isEmpty()) {
            playerStats.getBlockTargets().forEach((key, value) ->
                    ps.getBlockTargets().merge(key, value, Integer::sum)
            );
        }
        if (!playerStats.getChallengeTargets().isEmpty()) {
            playerStats.getChallengeTargets().forEach((key, value) ->
                    ps.getChallengeTargets().merge(key, value, Integer::sum)
            );
        }
        if(!playerStats.getPlayersKilled().isEmpty()) {
            for (String killedPlayerId : playerStats.getPlayersKilled()) {
                ps.getPlayersKilled().merge(killedPlayerId, 1, Integer::sum);
            }
        }

        // Survival
        ps.setTotalTurnsSurvived(ps.getTotalTurnsSurvived() + playerStats.getTurnsSurvived());
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

        return lifetimeStats;
    }
    public static double safeRate(int success, int attempts) {
        return attempts == 0 ? 0.0 : (double) success / attempts;
    }
    public static double calculateEntropy(PersonalityStats ps) {

        // Collect all action counts
        int[] counts = {
                ps.getIncomeCount(),
                ps.getTaxAttempts(),
                ps.getStealAttempts(),
                ps.getExchangeAttempts(),
                ps.getForeignAidAttempts(),
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
}
