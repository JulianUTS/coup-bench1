package com.example.coup_bench.model;

public class Action {

    private final ActionType type;
    private final boolean requiresTarget;
    private final boolean canBeBlocked;
    private final boolean canBeChallenged;

    public Action(ActionType type, boolean requiresTarget, boolean canBeBlocked, boolean canBeChallenged) {
        this.type = type;
        this.requiresTarget = requiresTarget;
        this.canBeBlocked = canBeBlocked;
        this.canBeChallenged = canBeChallenged;
    }

    public ActionType getType() {
        return type;
    }

    public boolean requiresTarget() {
        return requiresTarget;
    }

    public boolean canBeBlocked() {
        return canBeBlocked;
    }

    public boolean canBeChallenged() {
        return canBeChallenged;
    }
}

