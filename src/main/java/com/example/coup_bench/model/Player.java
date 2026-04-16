package com.example.coup_bench.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private String id;
    private String name;
    private String provider;

    private int coins = 2;
    private List<Card> cards = new ArrayList<>();

    public Player() {}  // REQUIRED

    public Player(String id, String name, String provider) {
        this.id = id;
        this.name = name;
        this.provider = provider;
    }

    public void addCard(Card c) {
        cards.add(c);
    }

    public void addCoins(int n) {
        coins += n;
    }

    public void removeCoins(int n) {
        coins = Math.max(0, coins - n);
    }

    public boolean isAlive() {
        return cards.stream().anyMatch(c -> !c.isRevealed());
    }

    public void revealAny() {
        cards.stream()
                .filter(c -> !c.isRevealed())
                .findFirst()
                .ifPresent(Card::reveal);
    }

    // Getters + setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getProvider() { return provider; }
    public int getCoins() { return coins; }
    public List<Card> getCards() { return cards; }
}

