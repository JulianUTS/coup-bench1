package com.example.coup_bench.model.repoModels;

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

    private int incomeCount;
    private int taxCount;
    private int stealAttempts;
    private int stealSuccesses;
    private int assassinationAttempts;
    private int coupsPerformed;

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

    public int getIncomeCount() {
        return incomeCount;
    }

    public void setIncomeCount(int incomeCount) {
        this.incomeCount = incomeCount;
    }

    public int getTaxCount() {
        return taxCount;
    }

    public void setTaxCount(int taxCount) {
        this.taxCount = taxCount;
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

    public int getCoupsPerformed() {
        return coupsPerformed;
    }

    public void setCoupsPerformed(int coupsPerformed) {
        this.coupsPerformed = coupsPerformed;
    }
}


