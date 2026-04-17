package com.example.coup_bench.model;

public class AiDecision {
    public ActionType action;   // e.g. TAX, STEAL, INCOME
    public String targetId;     // null if no target
    public boolean block;       // true if blocking
    public boolean challenge;   // true if challenging
    public String reason;
}

