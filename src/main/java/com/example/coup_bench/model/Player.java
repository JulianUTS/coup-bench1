package com.example.coup_bench.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String id;
    private final String provider;
    private final String personality;

    private int coins = 2;
    private final List<Card> cards = new ArrayList<>();

    public Player(String id, String provider, String personality) {
        this.id = id;
        this.provider = provider;
        this.personality = personality;
    }

    public String getId() { return id; }
    public String getProvider() { return provider; }
    public String getPersonality() { return personality; }

    public int getCoins() { return coins; }
    public List<Card> getCards() { return cards; }

    public void addCoins(int amount) { coins += amount; }
    public void removeCoins(int amount) { coins = Math.max(0, coins - amount); }

    public void addCard(Card card) { cards.add(card); }

    public boolean isAlive() {
        return cards.stream().anyMatch(c -> !c.isRevealed());
    }
    private final PlayerMemory memory = new PlayerMemory();

    public PlayerMemory getMemory() { return memory; }

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

    public Card removeCard(CardType type) {
        for (Card c : cards) {
            if (c.getType() == type && !c.isRevealed()) {
                c.reveal();       // mark as revealed
                cards.remove(c);  // remove from player's hand
                return c;         // return so Game can discard it
            }
        }
        throw new IllegalStateException("Player does not have an unrevealed " + type + " to remove");
    }


    public boolean hasCard(CardType type) {
        return cards.stream()
                .anyMatch(c -> c.getType() == type && !c.isRevealed());
    }
}
