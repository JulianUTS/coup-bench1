package com.example.coup_bench.model;

import com.example.coup_bench.model.Enums.CardType;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String id;
    private final String provider;
    private final String personality;

    private boolean alive = true;
    private int coins = 2;
    private final List<CardType> cards = new ArrayList<>();
    private int bluffsAttempted = 0;
    private int bluffsSuccessful = 0;
    private int bluffsFailed = 0;

    private int challengesIssued = 0;
    private int challengesWon = 0;
    private int challengesLost = 0;

    private int blocksIssued = 0;
    private int blocksSuccessful = 0;
    private int blocksFailed = 0;

    private int incomeCount = 0;
    private int taxCount = 0;
    private int stealAttempts = 0;
    private int stealSuccesses = 0;
    private int assassinationAttempts = 0;
    private int coupsPerformed = 0;

    public Player(String id, String provider, String personality) {
        this.id = id;
        this.provider = provider;
        this.personality = personality;
    }



    public void incrementBluffsAttempted() {
        this.bluffsAttempted++;
    }

    public int getBluffsSuccessful() {
        return bluffsSuccessful;
    }

    public void incrementBluffsSuccessful() {
        this.bluffsSuccessful++;
    }

    public int getBluffsFailed() {
        return bluffsFailed;
    }

    public void incrementBluffsFailed() {
        this.bluffsFailed++;
    }

    public int getChallengesIssued() {
        return challengesIssued;
    }

    public void incrementChallengesIssued() {
        this.challengesIssued++;
    }

    public int getChallengesWon() {
        return challengesWon;
    }

    public void incrementChallengesWon() {
        this.challengesWon++;
    }

    public int getChallengesLost() {
        return challengesLost;
    }

    public void incrementChallengesLost() {
        this.challengesLost++;
    }

    public int getBlocksIssued() {
        return blocksIssued;
    }

    public void incrementBlocksIssued() {
        this.blocksIssued++;
    }

    public int getBlocksSuccessful() {
        return blocksSuccessful;
    }

    public void incrementBlocksSuccessful() {
        this.blocksSuccessful++;
    }

    public int getBlocksFailed() {
        return blocksFailed;
    }

    public void incrementBlocksFailed() {
        this.blocksFailed++;
    }

    public int getIncomeCount() {
        return incomeCount;
    }

    public void incrementIncomeCount() {
        this.incomeCount++;
    }

    public int getTaxCount() {
        return taxCount;
    }

    public void incrementTaxCount() {
        this.taxCount++;
    }

    public int getStealAttempts() {
        return stealAttempts;
    }

    public void incrementStealAttempts() {
        this.stealAttempts++;
    }

    public int getStealSuccesses() {
        return stealSuccesses;
    }

    public void incrementStealSuccesses() {
        this.stealSuccesses++;
    }

    public int getAssassinationAttempts() {
        return assassinationAttempts;
    }

    public void incrementAssassinationAttempts() {
        this.assassinationAttempts++;
    }

    public int getCoupsPerformed() {
        return coupsPerformed;
    }

    public void incrementCoupsPerformed() {
        this.coupsPerformed++;
    }


    public String getId() { return id; }
    public String getProvider() { return provider; }
    public String getPersonality() { return personality; }

    public int getCoins() { return coins; }
    public List<CardType> getCards() { return cards; }


    public void addCoins(int amount) { coins += amount; }
    public void removeCoins(int amount) { coins = Math.max(0, coins - amount); }

    public boolean isAlive() {
        return (alive);
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
                if(cards.isEmpty()) alive = false;{

                }
                return cardToRemove;
            }
        }
        throw new IllegalStateException("Player does not have "  + cardToRemove + " to remove");
    }


    public boolean hasCard(CardType type) {
        return cards.contains(type);
    }
}
