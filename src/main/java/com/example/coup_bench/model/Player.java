package com.example.coup_bench.model;

import com.example.coup_bench.model.Enums.CardType;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String id;
    private final String personality;

    private int coins = 2;
    private final List<CardType> cards = new ArrayList<>();
    private final PlayerStats playerStats = new  PlayerStats();




    public Player(String id, String personality) {
        this.id = id;
        this.personality = personality;
    }

    public String getId() { return id; }
    public String getPersonality() { return personality; }
    public int getCoins() { return coins; }
    public List<CardType> getCards() { return cards; }
    public void addCoins(int amount) {
        coins += amount;
        playerStats.setTotalCoinGained(amount);
    }
    public void removeCoins(int amount) {
        coins = Math.max(0, coins - amount);
    playerStats.setTotalCoinsSpent(amount);}
    public boolean isAlive() {
        return !cards.isEmpty();
    }

    public boolean hasCard(CardType type) {
        return cards.contains(type);
    }


    public PlayerStats getPlayerStats() {
        return playerStats;
    }
    public boolean isHuman() {
        return this.id.equals("human");
    }
}
