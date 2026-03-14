package com.example.coup_bench.model;

import java.util.*;

public class Game {

    private final String id;
    private final List<Player> players = new ArrayList<>();
    private final Deque<Card> deck = new ArrayDeque<>();
    private final List<Card> discardPile = new ArrayList<>();

    private int currentPlayerIndex = 0;
    private GameState state = GameState.WAITING_FOR_PLAYERS;

    private ActionType declaredAction;
    private String actingPlayerId;
    private String targetPlayerId;

    public Game(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameState getState() {
        return state;
    }

    public void addPlayer(Player player) {
        if (state != GameState.WAITING_FOR_PLAYERS)
            throw new IllegalStateException("Game already started");

        players.add(player);
    }

    public void startGame() {
        initializeDeck();
        dealCards();
        state = GameState.IN_PROGRESS;
    }

    private void initializeDeck() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            cards.add(new Card(CardType.DUKE));
            cards.add(new Card(CardType.ASSASSIN));
            cards.add(new Card(CardType.CAPTAIN));
            cards.add(new Card(CardType.AMBASSADOR));
            cards.add(new Card(CardType.CONTESSA));
        }
        Collections.shuffle(cards);
        deck.addAll(cards);
    }

    private void dealCards() {
        for (Player p : players) {
            p.addCard(deck.pop());
            p.addCard(deck.pop());
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isAlive());
    }

    public Card drawCard() {
        return deck.pop();
    }

    public void discard(Card card) {
        discardPile.add(card);
    }

    public void declareAction(String playerId, ActionType action, String targetId) {
        this.actingPlayerId = playerId;
        this.declaredAction = action;
        this.targetPlayerId = targetId;
        this.state = GameState.ACTION_DECLARED;
    }
}

