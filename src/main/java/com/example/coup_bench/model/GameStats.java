package com.example.coup_bench.model;

import com.example.coup_bench.model.repoModels.InteractionRecord;
import com.example.coup_bench.model.repoModels.TurnSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameStats {
    private final List<ActionRecord> bluffLog= new ArrayList<>();
    private final List<ActionRecord> challengeLog= new ArrayList<>();
    private final List<InteractionRecord> interactionLog= new ArrayList<>();
    private final List<TurnSnapshot> turnSnapshotLog= new ArrayList<>();
    private int TotalBlocks = 0;
    private int TotalChallenges = 0;
    private final List<Integer> coinsPerTurn = new ArrayList<>();
    private final List<Integer> bluffsPerTurn = new ArrayList<>();

    public List<TurnSnapshot> getTurnSnapshotLog() {
        return turnSnapshotLog;
    }

    public List<InteractionRecord> getInteractionLog() {
        return interactionLog;
    }
    public void logTurnSnapshot(TurnSnapshot turnSnapshot) {
        this.turnSnapshotLog.add(turnSnapshot);
    }
    public void logInteraction(InteractionRecord interactionRecord) {
        this.interactionLog.add(interactionRecord);
    }

    public List<ActionRecord> getBluffLog() {
        return this.bluffLog;
    }
    public List<ActionRecord> getChallengeLog() {
        return this.challengeLog;
    }

    public void logBluff(ActionRecord record){
        this.bluffLog.add(record);
    }
    public void logChallenge(ActionRecord record){
        this.challengeLog.add(record);
    }

    public int getTotalBlocks() {
        return TotalBlocks;
    }
    public int getTotalChallenges() {
        return TotalChallenges;
    }

    public void incrementTotalBlocks() {
        TotalBlocks++;
    }
    public void incrementTotalChallenges() {
        TotalChallenges++;
    }


    public List<Integer> getCoinsPerTurn() {
        return coinsPerTurn;
    }
    public void addCoinsPerTurn(int totalCoins){
        coinsPerTurn.add(totalCoins);
    }

    public List<Integer> getBluffsPerTurn() {
        return bluffsPerTurn;
    }

    public void addBluffsPerTurn(int totalBluffs){
        bluffsPerTurn.add(totalBluffs);
    }
}
