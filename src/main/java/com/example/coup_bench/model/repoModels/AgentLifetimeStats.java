package com.example.coup_bench.model.repoModels;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "agent_stats")
public class AgentLifetimeStats {

    @Id
    private String provider;

    private int totalGames;
    private int wins;
    private int losses;

    // Aggression
    private int totalStealAttempts;
    private int totalAssassinationAttempts;
    private int totalCoupsPerformed;

    // Risk
    private int totalBluffsAttempted;
    private int totalChallengesIssued;

    // Defense
    private int totalBlocksIssued;

    // Survival
    private int totalTurnsSurvived;
    private int totalTurnsPlayed;
    private double averageSurvivalRate;

    // Game flow
    private long totalGameDurationSec;
    private double averageGameDurationSec;

    // Interaction heatmaps
    private Map<String, Integer> targetedProviders = new HashMap<>();
    private Map<String, Integer> challengedProviders = new HashMap<>();

    private Map<String, PersonalityStats> personalities = new HashMap<>();

    public AgentLifetimeStats() {}

    public AgentLifetimeStats(String provider) {
        this.provider = provider;
    }

    // -------- Getters & Setters --------

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public Map<String, PersonalityStats> getPersonalities() {
        return personalities;
    }

    public void setPersonalities(Map<String, PersonalityStats> personalities) {
        this.personalities = personalities;
    }
    // Aggression
    public int getTotalStealAttempts() { return totalStealAttempts; }
    public void setTotalStealAttempts(int totalStealAttempts) { this.totalStealAttempts = totalStealAttempts; }

    public int getTotalAssassinationAttempts() { return totalAssassinationAttempts; }
    public void setTotalAssassinationAttempts(int totalAssassinationAttempts) { this.totalAssassinationAttempts = totalAssassinationAttempts; }

    public int getTotalCoupsPerformed() { return totalCoupsPerformed; }
    public void setTotalCoupsPerformed(int totalCoupsPerformed) { this.totalCoupsPerformed = totalCoupsPerformed; }

    // Risk
    public int getTotalBluffsAttempted() { return totalBluffsAttempted; }
    public void setTotalBluffsAttempted(int totalBluffsAttempted) { this.totalBluffsAttempted = totalBluffsAttempted; }

    public int getTotalChallengesIssued() { return totalChallengesIssued; }
    public void setTotalChallengesIssued(int totalChallengesIssued) { this.totalChallengesIssued = totalChallengesIssued; }

    // Defense
    public int getTotalBlocksIssued() { return totalBlocksIssued; }
    public void setTotalBlocksIssued(int totalBlocksIssued) { this.totalBlocksIssued = totalBlocksIssued; }

    // Survival
    public int getTotalTurnsSurvived() { return totalTurnsSurvived; }
    public void setTotalTurnsSurvived(int totalTurnsSurvived) { this.totalTurnsSurvived = totalTurnsSurvived; }

    public int getTotalTurnsPlayed() { return totalTurnsPlayed; }
    public void setTotalTurnsPlayed(int totalTurnsPlayed) { this.totalTurnsPlayed = totalTurnsPlayed; }

    public double getAverageSurvivalRate() { return averageSurvivalRate; }
    public void setAverageSurvivalRate(double averageSurvivalRate) { this.averageSurvivalRate = averageSurvivalRate; }

    // Game flow
    public long getTotalGameDurationSec() { return totalGameDurationSec; }
    public void setTotalGameDurationMs(long totalGameDurationMs) { this.totalGameDurationSec = totalGameDurationMs; }

    public double getAverageGameDurationSec() { return averageGameDurationSec; }
    public void setAverageGameDurationMs(double averageGameDurationMs) { this.averageGameDurationSec = averageGameDurationMs; }

    // Interaction heatmaps
    public Map<String, Integer> getTargetedProviders() { return targetedProviders; }
    public void setTargetedProviders(Map<String, Integer> targetedProviders) { this.targetedProviders = targetedProviders; }

    public Map<String, Integer> getChallengedProviders() { return challengedProviders; }
    public void setChallengedProviders(Map<String, Integer> challengedProviders) { this.challengedProviders = challengedProviders; }
}

