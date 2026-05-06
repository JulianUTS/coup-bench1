package com.example.coup_bench.model;

import java.util.ArrayList;
import java.util.List;

public class PlayerStats {
    private int bluffsAttempted = 0;
    private int bluffsSuccessful = 0;
    private int bluffsFailed = 0;

    private int challengesIssued = 0;
    private int challengesWon = 0;
    private int challengesLost = 0;

    private int blocksIssued = 0;
    private int blocksSuccessful = 0;
    private int blocksFailed = 0;
    private int blocked= 0;

    private int incomeCount = 0;
    private int taxAttempts = 0;
    private int taxSuccessful = 0;
    private int exchangeAttempts = 0;
    private int exchangeSuccessful = 0;
    private int foreignAidAttempts = 0;
    private int foreignAidSuccessful = 0;
    private int stealAttempts = 0;
    private int stealSuccesses = 0;
    private int assassinationAttempts = 0;
    private int assassinationSuccesses = 0;
    private int coupsCount = 0;
    private int turnsSurvived = 0;
    private String killedBy;
    private final List<String> playersKilled = new ArrayList<>();
    private final List<String> actionTargets = new ArrayList<>();
    private final List<String> blockTargets = new ArrayList<>();
    private final List<String> challengeTargets = new ArrayList<>();

    // --- Action / Block / Challenge Targets ---
    public void addActionTarget(String targetId) {
        this.actionTargets.add(targetId);
    }

    public void addBlockTarget(String targetId) {
        this.blockTargets.add(targetId);
    }

    public void addChallengeTarget(String targetId) {
        this.challengeTargets.add(targetId);
    }

    // --- Bluffs ---
    public int getBluffsAttempted() { return bluffsAttempted; }
    public void incrementBluffsAttempted() { this.bluffsAttempted++; }

    public int getBluffsSuccessful() { return bluffsSuccessful; }
    public void incrementBluffsSuccessful() { this.bluffsSuccessful++; }

    public int getBluffsFailed() { return bluffsFailed; }
    public void incrementBluffsFailed() { this.bluffsFailed++; }

    // --- Challenges ---
    public int getChallengesIssued() { return challengesIssued; }
    public void incrementChallengesIssued() { this.challengesIssued++; }

    public int getChallengesWon() { return challengesWon; }
    public void incrementChallengesWon() { this.challengesWon++; }

    public int getChallengesLost() { return challengesLost; }
    public void incrementChallengesLost() { this.challengesLost++; }

    // --- Blocks ---
    public int getBlocksIssued() { return blocksIssued; }
    public void incrementBlocksIssued() { this.blocksIssued++; }

    public int getBlocksSuccessful() { return blocksSuccessful; }
    public void incrementBlocksSuccessful() { this.blocksSuccessful++; }

    public int getBlocksFailed() { return blocksFailed; }
    public void incrementBlocksFailed() { this.blocksFailed++; }

    public int getBlocked() { return blocked;}
    public void incrementedBlocked() { this.blocked++;}

    // --- Income / Tax ---
    public int getIncomeCount() { return incomeCount; }
    public void incrementIncomeCount() { this.incomeCount++; }

    public int getTaxAttempts() { return taxAttempts; }
    public void incrementTaxAttempts() {this.taxAttempts++;}

    public int getTaxSuccessful() { return taxSuccessful; }
    public void incrementTaxSuccesses() {
        this.taxSuccessful++;
    }

    // --- Steal ---
    public int getStealAttempts() { return stealAttempts; }
    public void incrementStealAttempts() { this.stealAttempts++; }

    public int getStealSuccesses() { return stealSuccesses; }
    public void incrementStealSuccesses() { this.stealSuccesses++; }

    // --- Assassination ---
    public int getAssassinationAttempts() { return assassinationAttempts; }
    public void incrementAssassinationAttempts() { this.assassinationAttempts++; }

    public int getAssassinationSuccesses() { return assassinationSuccesses; }
    public void incrementAssassinationSuccesses() { this.assassinationSuccesses++; }

    // --- Coup ---
    public int getCoupsCount() { return coupsCount; }
    public void incrementCoupsCount() { this.coupsCount++; }

    // --- Exchange ---
    public int getExchangeAttempts() {return exchangeAttempts;}
    public void incrementExchangeAttempts() {this.exchangeAttempts++;}

    public int getExchangeSuccessful() {return exchangeSuccessful;}
    public void incrementExchangeSuccessful() {this.exchangeSuccessful++;}

    // --- Foreign Aid ---
    public int getForeignAidAttempts() {return foreignAidAttempts;}
    public void incrementForeignAidAttempts() {this.foreignAidAttempts++;}

    public int getForeignAidSuccessful() {return foreignAidSuccessful;}
    public void incrementForeignAidSuccessful() {this.foreignAidSuccessful++;}

    // --- Survival ---
    public int getTurnsSurvived() { return turnsSurvived; }
    public void incrementTurnsSurvived() { this.turnsSurvived++; }

    // --- Players Killed ---
    public List<String> getPlayersKilled() { return playersKilled; }

}
