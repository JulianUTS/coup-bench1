package com.example.coup_bench.model;


import com.example.coup_bench.model.Enums.ActionType;

public class ActionRecord {

    private final String playerId;
    private final ActionType action;
    private final String targetId;
    private final String description;

    public ActionRecord(String playerId, ActionType action, String targetId, String description) {
        this.playerId = playerId;
        this.action = action;
        this.targetId = targetId;
        this.description = description;
    }

    public String getPlayerId() {
        return playerId;
    }

    public ActionType getAction() {
        return action;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDescription() {
        return description;
    }
}

