package com.example.coup_bench.model;

import com.example.coup_bench.model.Enums.ActionType;

public class InvalidActionRecord {
    private final String playerId;
    private final ActionType action;
    private final String targetId;
    private final Boolean actionIsBluff;
    private final String description;
    private final String errorMessage;

    public InvalidActionRecord(String playerId, ActionType action, String targetId,
                               Boolean actionIsBluff, String description, String errorMessage) {
        this.playerId = playerId;
        this.action = action;
        this.targetId = targetId;
        this.actionIsBluff = actionIsBluff;
        this.description = description;
        this.errorMessage = errorMessage;

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

    public String getErrorMessage() {
        return errorMessage;
    }

    public Boolean getActionIsBluff() {
        return actionIsBluff;
    }
}
