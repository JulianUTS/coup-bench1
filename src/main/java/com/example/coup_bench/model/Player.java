package com.example.coup_bench.model;

import com.example.coup_bench.AiDecisionService;
import com.example.coup_bench.AiServices.MultiModelRouter;
import com.example.coup_bench.CoupService;

import java.util.ArrayList;
import java.util.List;

public class Player {



    private final String id;
    private final String provider;
    private final String personality;

    private int coins = 2;
    private final List<CardType> cards = new ArrayList<>();

    public Player(String id, String provider, String personality) {
        this.id = id;
        this.provider = provider;
        this.personality = personality;
    }

    public String getId() { return id; }
    public String getProvider() { return provider; }
    public String getPersonality() { return personality; }

    public int getCoins() { return coins; }
    public List<CardType> getCards() { return cards; }


    public void addCoins(int amount) { coins += amount; }
    public void removeCoins(int amount) { coins = Math.max(0, coins - amount); }

    public boolean isAlive() {
        return (!cards.isEmpty());
    }

    public void addCard(CardType card) {
        cards.add(card); }



    public CardType switchCard(CardType cardToAdd, CardType cardToRemove) {
        addCard(cardToAdd);
        //return card to deck
        return removeCard(cardToRemove);
    }

    public CardType removeCard(CardType cardToRemove) {
        for (CardType c : cards) {
            if (c.equals(cardToRemove)) {
                cards.remove(c);
                //return card to deck
                return cardToRemove;
            }
        }
        throw new IllegalStateException("Player does not have "  + cardToRemove + " to remove");
    }


    public boolean hasCard(CardType type) {
        return cards.contains(type);
    }
}
