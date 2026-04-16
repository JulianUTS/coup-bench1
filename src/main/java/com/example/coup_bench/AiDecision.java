package com.example.coup_bench;

import com.example.coup_bench.model.ActionType;

public class AiDecision {
    public ActionType action;   // e.g. TAX, STEAL, INCOME
    public String targetId;     // null if no target
    public boolean block;       // true if blocking
    public boolean challenge;   // true if challenging
}

