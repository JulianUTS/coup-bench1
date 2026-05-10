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
    private int totalIncomeCount;
    private int totalTaxAttempts;
    private int totalForeignAidAttempts;
    private int totalExchangeAttempts;
    private int totalStealAttempts;
    private int totalAssassinationAttempts;
    private int totalCoupCount;

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
    private Map<String, Integer> actionTargets = new HashMap<>();
    private Map<String, Integer> blockTargets = new HashMap<>();
    private Map<String, Integer> challengeTargets = new HashMap<>();
    private Map<String, Integer> killedBy = new HashMap<>();

    private Map<String, PersonalityStats> personalities = new HashMap<>();

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

// -------------------- Total Games / Wins / Losses --------------------

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

// -------------------- Aggression --------------------

    public int getTotalIncomeCount() {
        return totalIncomeCount;
    }

    public void setTotalIncomeCount(int totalIncomeCount) {
        this.totalIncomeCount = totalIncomeCount;
    }

    public int getTotalTaxAttempts() {
        return totalTaxAttempts;
    }

    public void setTotalTaxAttempts(int totalTaxAttempts) {
        this.totalTaxAttempts = totalTaxAttempts;
    }

    public int getTotalForeignAidAttempts() {
        return totalForeignAidAttempts;
    }

    public void setTotalForeignAidAttempts(int totalForeignAidAttempts) {
        this.totalForeignAidAttempts = totalForeignAidAttempts;
    }

    public int getTotalExchangeAttempts() {
        return totalExchangeAttempts;
    }

    public void setTotalExchangeAttempts(int totalExchangeAttempts) {
        this.totalExchangeAttempts = totalExchangeAttempts;
    }

    public int getTotalStealAttempts() {
        return totalStealAttempts;
    }

    public void setTotalStealAttempts(int totalStealAttempts) {
        this.totalStealAttempts = totalStealAttempts;
    }

    public int getTotalAssassinationAttempts() {
        return totalAssassinationAttempts;
    }

    public void setTotalAssassinationAttempts(int totalAssassinationAttempts) {
        this.totalAssassinationAttempts = totalAssassinationAttempts;
    }

    public int getTotalCoupCount() {
        return totalCoupCount;
    }

    public void setTotalCoupCount(int totalCoupCount) {
        this.totalCoupCount = totalCoupCount;
    }

// -------------------- Risk --------------------

    public int getTotalBluffsAttempted() {
        return totalBluffsAttempted;
    }

    public void setTotalBluffsAttempted(int totalBluffsAttempted) {
        this.totalBluffsAttempted = totalBluffsAttempted;
    }

    public int getTotalChallengesIssued() {
        return totalChallengesIssued;
    }

    public void setTotalChallengesIssued(int totalChallengesIssued) {
        this.totalChallengesIssued = totalChallengesIssued;
    }

// -------------------- Defense --------------------

    public int getTotalBlocksIssued() {
        return totalBlocksIssued;
    }

    public void setTotalBlocksIssued(int totalBlocksIssued) {
        this.totalBlocksIssued = totalBlocksIssued;
    }

// -------------------- Survival --------------------

    public int getTotalTurnsSurvived() {
        return totalTurnsSurvived;
    }

    public void setTotalTurnsSurvived(int totalTurnsSurvived) {
        this.totalTurnsSurvived = totalTurnsSurvived;
    }

    public int getTotalTurnsPlayed() {
        return totalTurnsPlayed;
    }

    public void setTotalTurnsPlayed(int totalTurnsPlayed) {
        this.totalTurnsPlayed = totalTurnsPlayed;
    }

    public double getAverageSurvivalRate() {
        return averageSurvivalRate;
    }

    public void setAverageSurvivalRate(double averageSurvivalRate) {
        this.averageSurvivalRate = averageSurvivalRate;
    }

// -------------------- Game Flow --------------------

    public long getTotalGameDurationSec() {
        return totalGameDurationSec;
    }

    public void setTotalGameDurationSec(long totalGameDurationSec) {
        this.totalGameDurationSec = totalGameDurationSec;
    }

    public double getAverageGameDurationSec() {
        return averageGameDurationSec;
    }

    public void setAverageGameDurationSec(double averageGameDurationSec) {
        this.averageGameDurationSec = averageGameDurationSec;
    }

// -------------------- Interaction Heatmaps --------------------

    public Map<String, Integer> getActionTargets() {
        return actionTargets;
    }

    public void setActionTargets(Map<String, Integer> actionTargets) {
        this.actionTargets = actionTargets;
    }

    public Map<String, Integer> getBlockTargets() {
        return blockTargets;
    }

    public void setBlockTargets(Map<String, Integer> blockTargets) {
        this.blockTargets = blockTargets;
    }

    public Map<String, Integer> getChallengeTargets() {
        return challengeTargets;
    }

    public void setChallengeTargets(Map<String, Integer> challengeTargets) {
        this.challengeTargets = challengeTargets;
    }

    public Map<String, Integer> getKilledBy() {
        return killedBy;
    }

    public void setKilledBy(Map<String, Integer> killedBy) {
        this.killedBy = killedBy;
    }

// -------------------- Personalities --------------------

    public Map<String, PersonalityStats> getPersonalities() {
        return personalities;
    }

    public void setPersonalities(Map<String, PersonalityStats> personalities) {
        this.personalities = personalities;
    }
}