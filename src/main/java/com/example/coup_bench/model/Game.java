package com.example.coup_bench.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
@Document(collection = "games")
public class Game {

    @Id
    private final String id;
    private final List<Player> players = new ArrayList<>();
    private final Deque<Card> deck = new ArrayDeque<>();
    private final List<Card> discardPile = new ArrayList<>();
    private final List<ActionRecord> actionLog = new ArrayList<>();

    private int currentPlayerIndex = 0;
    private GameState state = GameState.WAITING_FOR_PLAYERS;

    private ActionType declaredAction;
    private String actingPlayerId;
    private String targetPlayerId;

    private String blockingPlayerId;
    private CardType blockingRole;
    private String challengerId;

    public Game(String id) {
        this.id = id;
    }

    public List<ActionRecord> getActionLog() {
        return actionLog;
    }
    public void logAction(ActionRecord record) {
        actionLog.add(record);
    }
    public String getId() { return id; }
    public List<Player> getPlayers() { return players; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public ActionType getDeclaredAction() { return declaredAction; }
    public String getActingPlayerId() { return actingPlayerId; }
    public String getTargetPlayerId() { return targetPlayerId; }

    public String getBlockingPlayerId() { return blockingPlayerId; }
    public void setBlockingPlayerId(String id) { this.blockingPlayerId = id; }

    public CardType getBlockingRole() { return blockingRole; }
    public void setBlockingRole(CardType role) { this.blockingRole = role; }

    public String getChallengerId() { return challengerId; }
    public void setChallengerId(String id) { this.challengerId = id; }

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
        if (players.stream().filter(Player::isAlive).count() <= 1) {
            state = GameState.FINISHED;
            return;
        }
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isAlive());
    }

    public Card drawCard() { return deck.pop(); }
    public void discard(Card card) { discardPile.add(card); }

    public Player getPlayer(String id) {
        return players.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public void declareAction(String playerId, ActionType action, String targetId) {
        if (!getCurrentPlayer().getId().equals(playerId))
            throw new IllegalStateException("Not your turn");
        this.actingPlayerId = playerId;
        this.declaredAction = action;
        this.targetPlayerId = targetId;
        this.blockingPlayerId = null;
        this.blockingRole = null;
        this.challengerId = null;
        this.state = GameState.ACTION_DECLARED;
    }
}