package com.example.coup_bench.model;

import com.example.coup_bench.model.Enums.ActionType;

public class HumanReactionRequest {

    private ActionType reaction;    // BLOCK, CHALLENGE, DO_NOTHING
    private String targetId;        // optional

    public ActionType getReaction() {
        return reaction;
    }

    public void setReaction(ActionType reaction) {
        this.reaction = reaction;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}

