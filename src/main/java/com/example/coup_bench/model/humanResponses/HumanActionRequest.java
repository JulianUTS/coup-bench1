package com.example.coup_bench.model.humanResponses;

import com.example.coup_bench.model.Enums.ActionType;

public class HumanActionRequest {

    private ActionType action;    // INCOME, TAX, STEAL, etc.
    private String targetId;      // optional


    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}

