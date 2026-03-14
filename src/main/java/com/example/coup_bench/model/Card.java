package com.example.coup_bench.model;

public class Card {

    private final CardType type;
    private boolean revealed;

    public Card(CardType type) {
        this.type = type;
        this.revealed = false;
    }

    public CardType getType() {
        return type;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void reveal() {
        this.revealed = true;
    }
}


