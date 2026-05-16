package com.example.coup_bench.model.repoModels;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "agent_stats_mirror_trials")
public class AgentLifetimeStats {

    @Id
    private String provider;
    private int numberOfPlayers;

    // ============================================================
    //  CORE OUTCOMES
    // ============================================================
    private int totalGames;
    private int wins;
    private int losses;

    // ============================================================
    //  ACTIONS (ATTEMPT / SUCCESS / FAIL GROUPED)
    // ============================================================

    // INCOME
    private int totalIncomeCount;

    // TAX
    private int totalTaxAttempts;
    private int totalTaxSuccessful;
    private int totalTaxFailed;

    // FOREIGN AID
    private int totalForeignAidAttempts;
    private int totalForeignAidSuccessful;
    private int totalForeignAidBlocked;

    // EXCHANGE
    private int totalExchangeAttempts;
    private int totalExchangeSuccessful;
    private int totalExchangeFailed;

    // STEAL
    private int totalStealAttempts;
    private int totalStealSuccesses;
    private int totalStealFailed;

    // ASSASSINATE
    private int totalAssassinationAttempts;
    private int totalAssassinationSuccesses;
    private int totalAssassinationFailed;

    // COUP
    private int totalCoupCount;

    // ============================================================
    //  RISK BEHAVIOUR
    // ============================================================
    private int totalBluffsAttempted;
    private int totalBluffsSuccessful;
    private int totalBluffsFailed;
    private int totalChallengesIssued;
    private int totalChallengesWon;
    private int totalChallengesLost;

    // ============================================================
    //  DEFENSIVE BEHAVIOUR
    // ============================================================
    private int totalBlocksIssued;
    private int totalBlocksSuccessful;
    private int totalBlocksFailed;
    private int totalTimesBlocked;

    // ============================================================
    //  SURVIVAL
    // ============================================================
    private int totalTurnsSurvived;
    private int totalTurnsPlayed;
    private double averageSurvivalRate;

    // ============================================================
    //  ECONOMY
    // ============================================================
    private int totalCoinGained;
    private int totalCoinsSpent;

    // ============================================================
    //  GAME FLOW (OPTIONAL)
    // ============================================================
    private long totalGameDurationSec;
    private double averageGameDurationSec;

    // ============================================================
    //  INTERACTION HEATMAPS
    // ============================================================
    private Map<String, Integer> actionTargets = new HashMap<>();
    private Map<String, Integer> blockTargets = new HashMap<>();
    private Map<String, Integer> challengeTargets = new HashMap<>();
    private Map<String, Integer> playersKilled = new HashMap<>();
    private Map<String, Integer> killedBy = new HashMap<>();
    private Map<String, Integer> causeOfDeath = new HashMap<>();

    // ============================================================
    //  POSITIONAL PERFORMANCE
    // ============================================================
    private Map<Integer, Integer> winsFromSeatIndex = new HashMap<>();
    private Map<Integer, Integer> lossesFromSeatIndex = new HashMap<>();

    // ============================================================
    //  PERSONALITY BREAKDOWN
    // ============================================================
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

    public Map<String, Integer> getCauseOfDeath() {
        return causeOfDeath;
    }

    public void setCauseOfDeath(Map<String, Integer> causeOfDeath) {
        this.causeOfDeath = causeOfDeath;
    }

// -------------------- Personalities --------------------

    public Map<String, PersonalityStats> getPersonalities() {
        return personalities;
    }

    public void setPersonalities(Map<String, PersonalityStats> personalities) {
        this.personalities = personalities;
    }

    public Map<Integer, Integer> getWinsFromSeatIndex() {
        return winsFromSeatIndex;
    }

    public void setWinsFromSeatIndex(Map<Integer, Integer> winsFromSeatIndex) {
        this.winsFromSeatIndex = winsFromSeatIndex;
    }

    public Map<Integer, Integer> getLossesFromSeatIndex() {
        return lossesFromSeatIndex;
    }

    public void setLossesFromSeatIndex(Map<Integer, Integer> lossesFromSeatIndex) {
        this.lossesFromSeatIndex = lossesFromSeatIndex;
    }

    public int getTotalBlocksSuccessful() {
        return totalBlocksSuccessful;
    }

    public void setTotalBlocksSuccessful(int totalBlocksSuccessful) {
        this.totalBlocksSuccessful = totalBlocksSuccessful;
    }

    public int getTotalBlocksFailed() {
        return totalBlocksFailed;
    }

    public void setTotalBlocksFailed(int totalBlocksFailed) {
        this.totalBlocksFailed = totalBlocksFailed;
    }

    public int getTotalTimesBlocked() {
        return totalTimesBlocked;
    }

    public void setTotalTimesBlocked(int totalTimesBlocked) {
        this.totalTimesBlocked = totalTimesBlocked;
    }

    public int getTotalChallengesWon() {
        return totalChallengesWon;
    }

    public void setTotalChallengesWon(int totalChallengesWon) {
        this.totalChallengesWon = totalChallengesWon;
    }

    public int getTotalChallengesLost() {
        return totalChallengesLost;
    }

    public void setTotalChallengesLost(int totalChallengesLost) {
        this.totalChallengesLost = totalChallengesLost;
    }

    public int getTotalTaxSuccessful() {
        return totalTaxSuccessful;
    }

    public void setTotalTaxSuccessful(int totalTaxSuccessful) {
        this.totalTaxSuccessful = totalTaxSuccessful;
    }

    public int getTotalForeignAidSuccessful() {
        return totalForeignAidSuccessful;
    }

    public void setTotalForeignAidSuccessful(int totalForeignAidSuccessful) {
        this.totalForeignAidSuccessful = totalForeignAidSuccessful;
    }

    public int getTotalExchangeSuccessful() {
        return totalExchangeSuccessful;
    }

    public void setTotalExchangeSuccessful(int totalExchangeSuccessful) {
        this.totalExchangeSuccessful = totalExchangeSuccessful;
    }

    public int getTotalStealSuccesses() {
        return totalStealSuccesses;
    }

    public void setTotalStealSuccesses(int totalStealSuccesses) {
        this.totalStealSuccesses = totalStealSuccesses;
    }

    public int getTotalAssassinationSuccesses() {
        return totalAssassinationSuccesses;
    }

    public void setTotalAssassinationSuccesses(int totalAssassinationSuccesses) {
        this.totalAssassinationSuccesses = totalAssassinationSuccesses;
    }

    public int getTotalTaxFailed() {
        return totalTaxFailed;
    }

    public void setTotalTaxFailed(int totalTaxFailed) {
        this.totalTaxFailed = totalTaxFailed;
    }

    public int getTotalForeignAidBlocked() {
        return totalForeignAidBlocked;
    }

    public void setTotalForeignAidBlocked(int totalForeignAidBlocked) {
        this.totalForeignAidBlocked = totalForeignAidBlocked;
    }

    public int getTotalExchangeFailed() {
        return totalExchangeFailed;
    }

    public void setTotalExchangeFailed(int totalExchangeFailed) {
        this.totalExchangeFailed = totalExchangeFailed;
    }

    public int getTotalStealFailed() {
        return totalStealFailed;
    }

    public void setTotalStealFailed(int totalStealFailed) {
        this.totalStealFailed = totalStealFailed;
    }

    public int getTotalAssassinationFailed() {
        return totalAssassinationFailed;
    }

    public void setTotalAssassinationFailed(int totalAssassinationFailed) {
        this.totalAssassinationFailed = totalAssassinationFailed;
    }

    public Map<String, Integer> getPlayersKilled() {
        return playersKilled;
    }

    public void setPlayersKilled(Map<String, Integer> playersKilled) {
        this.playersKilled = playersKilled;
    }

    public int getTotalCoinGained() {
        return totalCoinGained;
    }

    public void setTotalCoinGained(int totalCoinGained) {
        this.totalCoinGained = totalCoinGained;
    }

    public int getTotalCoinsSpent() {
        return totalCoinsSpent;
    }

    public void setTotalCoinsSpent(int totalCoinsSpent) {
        this.totalCoinsSpent = totalCoinsSpent;
    }

    public int getTotalBluffsSuccessful() {
        return totalBluffsSuccessful;
    }

    public void setTotalBluffsSuccessful(int totalBluffsSuccessful) {
        this.totalBluffsSuccessful = totalBluffsSuccessful;
    }

    public int getTotalBluffsFailed() {
        return totalBluffsFailed;
    }

    public void setTotalBluffsFailed(int totalBluffsFailed) {
        this.totalBluffsFailed = totalBluffsFailed;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }
}