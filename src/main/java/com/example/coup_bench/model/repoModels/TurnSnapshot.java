package com.example.coup_bench.model.repoModels;

import java.util.Map;

public class TurnSnapshot {

    private final int turnNumber;
    private final Map<String, Integer> coins;
    private final Map<String, Integer> influence;
    private final String actionTaken;
    private final String actorId;
    private final String targetId;

    public TurnSnapshot(int turnNumber,
                        Map<String, Integer> coins,
                        Map<String, Integer> influence,
                        String actionTaken,
                        String actorId,
                        String targetId) {
        this.turnNumber = turnNumber;
        this.coins = coins;
        this.influence = influence;
        this.actionTaken = actionTaken;
        this.actorId = actorId;
        this.targetId = targetId;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public Map<String, Integer> getCoins() {
        return coins;
    }

    public Map<String, Integer> getInfluence() {
        return influence;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public String getActorId() {
        return actorId;
    }

    public String getTargetId() {
        return targetId;
    }
}


