package com.example.coup_bench.model.repoModels;

import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TurnSnapshot {

    private final int turnNumber;
    private final Map<String, Boolean> aliveStatus;
    private final Map<String, Integer> coins;
    private final Map<String, Integer> influence;
    private final Map<String, List<CardType>> cards;
    private final ActionRecord action;
    private final ActionRecord block;
    private final ActionRecord challenge;


    public TurnSnapshot(int turnNumber,
                        Map<String, Boolean> aliveStatus,
                        Map<String, Integer> coins,
                        Map<String, Integer> influence,
                        Map<String, List<CardType>> cards,
                        ActionRecord action,
                        ActionRecord block,
                        ActionRecord challenge) {
        this.turnNumber = turnNumber;
        this.aliveStatus = aliveStatus;
        this.coins = coins;
        this.influence = influence;
        this.cards = cards;
        this.action = action;
        this.block = block;
        this.challenge = challenge;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public Map<String, Integer> getCoins() {
        return coins;
    }

    public Map<String, Integer> getInfluence() {
        return influence;
    }

    public Map<String, List<CardType>> getCards() {
        return cards;
    }

    public ActionRecord getAction() {
        return action;
    }

    public Map<String, Boolean> getAliveStatus() {
        return aliveStatus;
    }

    public ActionRecord getBlock() {
        return block;
    }

    public ActionRecord getChallenge() {
        return challenge;
    }
}


