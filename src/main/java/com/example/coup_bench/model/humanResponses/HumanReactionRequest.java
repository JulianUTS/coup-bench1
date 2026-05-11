package com.example.coup_bench.model.humanResponses;

import com.example.coup_bench.model.Enums.ActionType;

public class HumanReactionRequest {
    private ActionType reaction;    // BLOCK, CHALLENGE, DO_NOTHING

    public ActionType getReaction() {
        return reaction;
    }

    public void setReaction(ActionType reaction) {
        this.reaction = reaction;
    }


}

