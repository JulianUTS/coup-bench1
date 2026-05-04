package com.example.coup_bench;

import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.model.repoModels.AgentLifetimeStats;
import com.example.coup_bench.model.repoModels.GameSummary;
import com.example.coup_bench.model.repoModels.PersonalityStats;
import com.example.coup_bench.repo.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class RepoHelperService {

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
        summary.setBluffLog(game.getGameAnalyticsService().getBluffLog());
        summary.setInteractions(game.getGameAnalyticsService().getInteractionLog());
        summary.setTurnSnapshots(game.getGameAnalyticsService().getTurnSnapshotLog());
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
    public long diffSeconds(long startMs) {
        long nowMs = System.currentTimeMillis();
        return (nowMs - startMs) / 1000;
    }
}
