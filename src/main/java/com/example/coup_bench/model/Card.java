package com.example.coup_bench.model;

public class Card {

    private CardType type;
    private boolean revealed = false;

    public Card() {}  // REQUIRED

    public Card(CardType type) {
        this.type = type;
    }

    public CardType getType() { return type; }
    public boolean isRevealed() { return revealed; }
    public void reveal() { this.revealed = true; }
}



