package com.example.coup_bench.model.repoModels;

import com.example.coup_bench.model.Enums.ActionType;

public class InteractionRecord {

    private final String actorId;
    private final String targetId;
    private final ActionType actionType;
    private final boolean success;

    public InteractionRecord(String actorId,
                             String targetId,
                             ActionType actionType,
                             boolean success) {
        this.actorId = actorId;
        this.targetId = targetId;
        this.actionType = actionType;
        this.success = success;
    }

    public String getActorId() {
        return actorId;
    }

    public String getTargetId() {
        return targetId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public boolean isSuccess() {
        return success;
    }
}


