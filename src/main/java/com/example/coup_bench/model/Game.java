package com.example.coup_bench.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "games")
public class Game {

    @Id
    private String id;

    private GameState state = GameState.WAITING_FOR_PLAYERS;

    private List<Player> players = new ArrayList<>();
    private List<Card> deck = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();

    private String actingPlayerId;
    private String targetPlayerId;
    private ActionType declaredAction;

    private String blockingPlayerId;
    private CardType blockingRole;

    private String challengerId;

    private List<ActionRecord> actionLog = new ArrayList<>();

    public Game() {}  // REQUIRED for MongoDB

    public Game(String id) {
        this.id = id;
        initializeDeck();
    }

    private void initializeDeck() {
        for (int i = 0; i < 3; i++) {
            deck.add(new Card(CardType.DUKE));
            deck.add(new Card(CardType.ASSASSIN));
            deck.add(new Card(CardType.CAPTAIN));
            deck.add(new Card(CardType.AMBASSADOR));
            deck.add(new Card(CardType.CONTESSA));
        }
        Collections.shuffle(deck);
    }

    public void addPlayer(Player p) {
        players.add(p);
        p.addCard(drawCard());
        p.addCard(drawCard());
    }

    public Card drawCard() {
        return deck.remove(deck.size() - 1);
    }

    public void declareAction(String playerId, ActionType action, String targetId) {
        this.actingPlayerId = playerId;
        this.declaredAction = action;
        this.targetPlayerId = targetId;
        this.state = GameState.ACTION_DECLARED;
    }

    public Player getCurrentPlayer() {
        if (actingPlayerId == null) return null;
        return getPlayer(actingPlayerId);
    }

    public void discard(Card c) {
        discardPile.add(c);
    }

    public Player getPlayer(String id) {
        return players.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public void logAction(ActionRecord record) {
        actionLog.add(record);
    }

    public void startGame() {
        state = GameState.IN_PROGRESS;
        actingPlayerId = players.get(0).getId();
    }

    public void nextTurn() {
        long alive = players.stream().filter(Player::isAlive).count();
        if (alive <= 1) {
            state = GameState.FINISHED;
            return;
        }

        int idx = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(actingPlayerId)) {
                idx = i;
                break;
            }
        }

        do {
            idx = (idx + 1) % players.size();
        } while (!players.get(idx).isAlive());

        actingPlayerId = players.get(idx).getId();
    }

    // Getters + setters (MongoDB requires them)
    public String getId() { return id; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public List<Player> getPlayers() { return players; }

    public String getActingPlayerId() { return actingPlayerId; }
    public String getTargetPlayerId() { return targetPlayerId; }
    public void setTargetPlayerId(String id) { this.targetPlayerId = id; }

    public ActionType getDeclaredAction() { return declaredAction; }
    public void setDeclaredAction(ActionType a) { this.declaredAction = a; }

    public String getBlockingPlayerId() { return blockingPlayerId; }
    public void setBlockingPlayerId(String id) { this.blockingPlayerId = id; }

    public CardType getBlockingRole() { return blockingRole; }
    public void setBlockingRole(CardType role) { this.blockingRole = role; }

    public String getChallengerId() { return challengerId; }
    public void setChallengerId(String id) { this.challengerId = id; }

    public List<ActionRecord> getActionLog() { return actionLog; }
}
