package com.example.coup_bench.model.repoModels;

import java.util.HashMap;
import java.util.Map;

public class PersonalityStats {

    private int totalGames;
    private int wins;
    private int losses;

    private int bluffsAttempted;
    private int bluffsSuccessful;
    private int bluffsFailed;

    private int challengesIssued;
    private int challengesWon;
    private int challengesLost;

    private int blocksIssued;
    private int blocksSuccessful;
    private int blocksFailed;
    private int timesBlocked;

    private int incomeCount;
    private int taxAttempts;
    private int taxSuccessful;
    private int exchangeAttempts;
    private int exchangeSuccessful;
    private int foreignAidAttempts;
    private int foreignAidSuccessful;
    private int stealAttempts;
    private int stealSuccesses;
    private int assassinationAttempts;
    private int assassinationSuccesses;
    private int coupsCount;

    private int totalCoinGained;
    private int totalCoinsSpent;
    private int netCoinFlow;
    private int averageCoinPerTurn;
    private Map<String, Integer> causeOfDeath = new HashMap<>();
    private Map<Integer, Integer> winsFromSeatIndex = new HashMap<>();
    private Map<Integer, Integer> lossesFromSeatIndex = new HashMap<>();

    private Map<String, Integer> actionTargets = new HashMap<>();
    private Map<String, Integer> blockTargets = new HashMap<>();
    private Map<String, Integer> challengeTargets = new HashMap<>();
    private Map<String, Integer> playersKilled = new HashMap<>();

    private int aggressionScore;          // steal + assassinate + coup
    private int riskScore;

    // bluffs + challenges

    private double bluffSuccessRate;
    private double challengeSuccessRate;
    private double blockSuccessRate;
    private double stealSuccessRate;
    private double assassinationSuccessRate;

    private int totalTurnsSurvived;
    private double averageTurnsSurvived;

    private double actionEntropy;





    public PersonalityStats() {}

    // -------- Getters & Setters --------

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

    public int getBluffsAttempted() {
        return bluffsAttempted;
    }

    public void setBluffsAttempted(int bluffsAttempted) {
        this.bluffsAttempted = bluffsAttempted;
    }

    public int getBluffsSuccessful() {
        return bluffsSuccessful;
    }

    public void setBluffsSuccessful(int bluffsSuccessful) {
        this.bluffsSuccessful = bluffsSuccessful;
    }

    public int getBluffsFailed() {
        return bluffsFailed;
    }

    public void setBluffsFailed(int bluffsFailed) {
        this.bluffsFailed = bluffsFailed;
    }

    public int getChallengesIssued() {
        return challengesIssued;
    }

    public void setChallengesIssued(int challengesIssued) {
        this.challengesIssued = challengesIssued;
    }

    public int getChallengesWon() {
        return challengesWon;
    }

    public void setChallengesWon(int challengesWon) {
        this.challengesWon = challengesWon;
    }

    public int getChallengesLost() {
        return challengesLost;
    }

    public void setChallengesLost(int challengesLost) {
        this.challengesLost = challengesLost;
    }

    public int getBlocksIssued() {
        return blocksIssued;
    }

    public void setBlocksIssued(int blocksIssued) {
        this.blocksIssued = blocksIssued;
    }

    public int getBlocksSuccessful() {
        return blocksSuccessful;
    }

    public void setBlocksSuccessful(int blocksSuccessful) {
        this.blocksSuccessful = blocksSuccessful;
    }

    public int getBlocksFailed() {
        return blocksFailed;
    }

    public void setBlocksFailed(int blocksFailed) {
        this.blocksFailed = blocksFailed;
    }

    public int getTimesBlocked() {
        return timesBlocked;
    }
    public void setTimesBlocked(int timesBlocked) {
        this.timesBlocked = timesBlocked;
    }

    public int getIncomeCount() {
        return incomeCount;
    }

    public void setIncomeCount(int incomeCount) {
        this.incomeCount = incomeCount;
    }

    public int getTaxAttempts() {
        return taxAttempts;
    }

    public void setTaxAttempts(int taxAttempts) {
        this.taxAttempts = taxAttempts;
    }

    public int getTaxSuccessful() {
        return taxSuccessful;
    }

    public void setTaxSuccessful(int taxSuccessful) {
        this.taxSuccessful = taxSuccessful;
    }

    public int getExchangeAttempts() {
        return exchangeAttempts;
    }

    public void setExchangeAttempts(int exchangeAttempts) {
        this.exchangeAttempts = exchangeAttempts;
    }

    public int getExchangeSuccessful() {
        return exchangeSuccessful;
    }

    public void setExchangeSuccessful(int exchangeSuccessful) {
        this.exchangeSuccessful = exchangeSuccessful;
    }

    public int getForeignAidAttempts() {
        return foreignAidAttempts;
    }

    public void setForeignAidAttempts(int foreignAidAttempts) {
        this.foreignAidAttempts = foreignAidAttempts;
    }

    public int getForeignAidSuccessful() {
        return foreignAidSuccessful;
    }

    public void setForeignAidSuccessful(int foreignAidSuccessful) {
        this.foreignAidSuccessful = foreignAidSuccessful;
    }


    public int getStealAttempts() {
        return stealAttempts;
    }

    public void setStealAttempts(int stealAttempts) {
        this.stealAttempts = stealAttempts;
    }

    public int getStealSuccesses() {
        return stealSuccesses;
    }

    public void setStealSuccesses(int stealSuccesses) {
        this.stealSuccesses = stealSuccesses;
    }

    public int getAssassinationAttempts() {
        return assassinationAttempts;
    }

    public void setAssassinationAttempts(int assassinationAttempts) {
        this.assassinationAttempts = assassinationAttempts;
    }

    public int getAssassinationSuccesses() {
        return assassinationSuccesses;
    }

    public void setAssassinationSuccesses(int assassinationSuccesses) {
        this.assassinationSuccesses = assassinationSuccesses;
    }

    public int getCoupsPerformed() {
        return coupsCount;
    }

    public void setCoupsPerformed(int coupsPerformed) {
        this.coupsCount = coupsPerformed;
    }

    public void setAggressionScore(int aggressionScore) { this.aggressionScore = aggressionScore; }

    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public void setBluffSuccessRate(double bluffSuccessRate) { this.bluffSuccessRate = bluffSuccessRate; }

    public void setChallengeSuccessRate(double challengeSuccessRate) { this.challengeSuccessRate = challengeSuccessRate; }

    public void setBlockSuccessRate(double blockSuccessRate) { this.blockSuccessRate = blockSuccessRate; }

    public void setStealSuccessRate(double stealSuccessRate) { this.stealSuccessRate = stealSuccessRate; }

    public void setAssassinationSuccessRate(double assassinationSuccessRate) { this.assassinationSuccessRate = assassinationSuccessRate; }

    public int getTotalTurnsSurvived() { return totalTurnsSurvived; }
    public void setTotalTurnsSurvived(int totalTurnsSurvived) { this.totalTurnsSurvived = totalTurnsSurvived; }

    public void setAverageTurnsSurvived(double averageTurnsSurvived) { this.averageTurnsSurvived = averageTurnsSurvived; }

    public void setActionEntropy(double actionEntropy) { this.actionEntropy = actionEntropy; }

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

    public int getNetCoinFlow() {
        return netCoinFlow;
    }

    public void setNetCoinFlow(int netCoinFlow) {
        this.netCoinFlow = netCoinFlow;
    }

    public int getAverageCoinPerTurn() {
        return averageCoinPerTurn;
    }

    public void setAverageCoinPerTurn(int averageCoinPerTurn) {
        this.averageCoinPerTurn = averageCoinPerTurn;
    }

    public Map<String, Integer> getCauseOfDeath() {
        return causeOfDeath;
    }

    public void setCauseOfDeath(Map<String, Integer> causeOfDeath) {
        this.causeOfDeath = causeOfDeath;
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

    public Map<String, Integer> getPlayersKilled() {
        return playersKilled;
    }

    public void setPlayersKilled(Map<String, Integer> playersKilled) {
        this.playersKilled = playersKilled;
    }
}


