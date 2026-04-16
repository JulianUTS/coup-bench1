package com.example.coup_bench.model;

public class ActionRecord {

    private String playerId;
    private ActionType action;
    private String targetId;
    private String description;

    public ActionRecord() {}  // REQUIRED

    public ActionRecord(String playerId, ActionType action, String targetId, String description) {
        this.playerId = playerId;
        this.action = action;
        this.targetId = targetId;
        this.description = description;
    }

    public String getPlayerId() { return playerId; }
    public ActionType getAction() { return action; }
    public String getTargetId() { return targetId; }
    public String getDescription() { return description; }
}


