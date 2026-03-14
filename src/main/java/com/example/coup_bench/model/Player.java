package com.example.coup_bench.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String id;
    private final String name;
    private int coins;
    private final List<Card> cards = new ArrayList<>();

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.coins = 2; // Coup starting coins
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public void removeCoins(int amount) {
        coins -= amount;
        if (coins < 0) coins = 0;
    }

    public List<Card> getCards() {
        return cards;
    }

    public boolean isAlive() {
        return cards.stream().anyMatch(c -> !c.isRevealed());
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void revealCard(CardType type) {
        cards.stream()
                .filter(c -> c.getType() == type && !c.isRevealed())
                .findFirst()
                .ifPresent(Card::reveal);
    }
}
