package com.example.coup_bench.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String id;
    private final String name;
    private final String provider; // <-- ADD THIS
    private int coins = 2;
    private final List<Card> cards = new ArrayList<>();

    public Player(String id, String name, String provider) {
        this.id = id;
        this.name = name;
        this.provider = provider;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getProvider() { return provider; } // <-- ADD GETTER
    public int getCoins() { return coins; }
    public List<Card> getCards() { return cards; }

    public void addCoins(int amount) { coins += amount; }
    public void removeCoins(int amount) { coins = Math.max(0, coins - amount); }

    public void addCard(Card card) { cards.add(card); }

    public boolean isAlive() {
        return cards.stream().anyMatch(c -> !c.isRevealed());
    }

    public void revealAny() {
        cards.stream()
                .filter(c -> !c.isRevealed())
                .findFirst()
                .ifPresent(Card::reveal);
    }

    public void revealCard(CardType type) {
        cards.stream()
                .filter(c -> c.getType() == type && !c.isRevealed())
                .findFirst()
                .ifPresent(Card::reveal);
    }
}
