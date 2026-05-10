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

// Timestamps

        summary.setTotalGameDurationSec(diffSeconds(game.getTimestampStart()));


// Game stats
        summary.setNumberOfPlayers(game.getPlayers().size());
        summary.setWinnerId(game.getWinnerId(game));
        summary.setTotalTurns(game.getTurn());
        summary.setTotalActions(game.getActionLog().size());
        summary.setTotalChallenges(gameStats.getTotalBlocks());
        summary.setTotalBlocks(gameStats.getTotalChallenges());
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

        return summary;
    }

    public static AgentLifetimeStats getAgentLifetimeStats(Player player, PlayerRepository playerRepo, GameSummary gameSummary) {
        String provider = player.getId();
        PlayerStats playerStats = player.getPlayerStats();
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
        stats.setTotalStealAttempts(stats.getTotalStealAttempts() + playerStats.getStealAttempts());
        stats.setTotalAssassinationAttempts(stats.getTotalAssassinationAttempts() + playerStats.getAssassinationAttempts());
        stats.setTotalCoupCount(stats.getTotalCoupCount() + playerStats.getCoupsCount());
        stats.setTotalTaxAttempts(stats.getTotalTaxAttempts() + playerStats.getTaxAttempts());
        stats.setTotalForeignAidAttempts(stats.getTotalForeignAidAttempts() + playerStats.getForeignAidAttempts());
        stats.setTotalExchangeAttempts(stats.getTotalExchangeAttempts() + playerStats.getExchangeAttempts());
        stats.setTotalIncomeCount(stats.getTotalIncomeCount() + playerStats.getIncomeCount());

        // Risk
        stats.setTotalBluffsAttempted(stats.getTotalBluffsAttempted() + playerStats.getBluffsAttempted());
        stats.setTotalChallengesIssued(stats.getTotalChallengesIssued() + playerStats.getChallengesIssued());

        // Defense
        stats.setTotalBlocksIssued(stats.getTotalBlocksIssued() + playerStats.getBlocksIssued());

        // Survival
        stats.setTotalTurnsSurvived(stats.getTotalTurnsSurvived() + playerStats.getTurnsSurvived());
        stats.setTotalTurnsPlayed(stats.getTotalTurnsPlayed() + gameSummary.getTotalTurns());

        // Game duration
        stats.setTotalGameDurationSec(stats.getTotalGameDurationSec() + gameSummary.getTotalGameDurationSec());

        // Interaction heatmaps
        if (!playerStats.getActionTargets().isEmpty()) {
            stats.getActionTargets().forEach((key, value) ->
                    playerStats.getActionTargets().merge(key, value, Integer::sum)
            );
        }

        if (!playerStats.getBlockTargets().isEmpty()) {
            stats.getBlockTargets().forEach((key, value) ->
                    playerStats.getBlockTargets().merge(key, value, Integer::sum)
            );
        }
        if (!playerStats.getChallengeTargets().isEmpty()) {
            stats.getChallengeTargets().forEach((key, value) ->
                    playerStats.getChallengeTargets().merge(key, value, Integer::sum)
            );
        }
        if(playerStats.getKilledBy() != null) {
            stats.getKilledBy().merge(playerStats.getKilledBy(), 1, Integer::sum);
        }

        // Recompute provider averages
        stats.setAverageSurvivalRate(
                safeRate(stats.getTotalTurnsSurvived(), stats.getTotalTurnsPlayed())
        );

        stats.setAverageGameDurationSec(
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
        ps.setBluffsAttempted(ps.getBluffsAttempted() + playerStats.getBluffsAttempted());
        ps.setBluffsSuccessful(ps.getBluffsSuccessful() + playerStats.getBluffsSuccessful());
        ps.setBluffsFailed(ps.getBluffsFailed() + playerStats.getBluffsFailed());

        ps.setChallengesIssued(ps.getChallengesIssued() + playerStats.getChallengesIssued());
        ps.setChallengesWon(ps.getChallengesWon() + playerStats.getChallengesWon());
        ps.setChallengesLost(ps.getChallengesLost() + playerStats.getChallengesLost());

        ps.setBlocksIssued(ps.getBlocksIssued() + playerStats.getBlocksIssued());
        ps.setBlocksSuccessful(ps.getBlocksSuccessful() + playerStats.getBlocksSuccessful());
        ps.setBlocksFailed(ps.getBlocksFailed() + playerStats.getBlocksFailed());

        ps.setIncomeCount(ps.getIncomeCount() + playerStats.getIncomeCount());
        ps.setTaxAttempts(ps.getTaxAttempts() + playerStats.getTaxSuccessful());
        ps.setTaxSuccessful(ps.getTaxSuccessful() + playerStats.getTaxSuccessful());
        ps.setExchangeAttempts(ps.getExchangeAttempts() + playerStats.getExchangeSuccessful());
        ps.setExchangeSuccessful(ps.getExchangeSuccessful() + playerStats.getExchangeSuccessful());
        ps.setForeignAidAttempts(ps.getForeignAidAttempts() + playerStats.getForeignAidSuccessful());
        ps.setForeignAidSuccessful(ps.getForeignAidSuccessful() + playerStats.getForeignAidSuccessful());


        ps.setStealAttempts(ps.getStealAttempts() + playerStats.getStealAttempts());
        ps.setStealSuccesses(ps.getStealSuccesses() + playerStats.getStealSuccesses());

        ps.setAssassinationAttempts(ps.getAssassinationAttempts() + playerStats.getAssassinationAttempts());
        ps.setAssassinationSuccesses(ps.getAssassinationSuccesses() + playerStats.getAssassinationSuccesses());

        ps.setCoupsPerformed(ps.getCoupsPerformed() + playerStats.getCoupsCount());

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

        return stats;
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
