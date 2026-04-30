package com.example.coup_bench.model;

import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Enums.GameState;

import java.util.*;

public class Game {

    private final String id;
    private final List<Player> players = new ArrayList<>();
    private final Deque<CardType> deck = new ArrayDeque<>();
    private final List<ActionRecord> actionLog = new ArrayList<>();
    private final List<String> gameMemory = new ArrayList<>();
    private final List<InvalidActionRecord> invalidActionLog = new ArrayList<>();
    private final long timestampStart = System.currentTimeMillis();


    private int currentPlayerIndex = 0;
    private GameState state = GameState.WAITING_FOR_PLAYERS;
    private int invalidAction = 0;
    private int turn = 1;
    private int TotalBlocks = 0;
    private int TotalChallenges = 0;

    private ActionType declaredAction;
    private String actingPlayerId;
    private String targetId;

    private String blockerId;
    private CardType blockingRole;
    private String challengerId;

    public Game(String id) {
        this.id = id;
    }

    public int getTurn() {
        return turn;
    }
    public int getTotalBlocks() {
        return TotalBlocks;
    }
    public int getTotalChallenges() {
        return TotalChallenges;
    }

    public void incrementTotalBlocks() {
        TotalBlocks++;
    }
    public void incrementTotalChallenges() {
        TotalChallenges++;
    }
    public List<ActionRecord> getActionLog() {
        return actionLog;
    }
    public List<InvalidActionRecord> getInvalidActionLog() {
        return invalidActionLog;
    }
    public long getTimestampStart() {return timestampStart;}
    public List<String> getGameMemory() {
        return gameMemory;
    }

    public int getInvalidAction() {
        return invalidAction;
    }
    public void incrementInvalidAction() {
        invalidAction++;
    }
    public void resetInvalidAction() {
        invalidAction = 0;
    }
    public void logGameMemory(String memory) {
        System.out.println(memory);
        gameMemory.add(memory);
    }

    public void logAction(ActionRecord record) {
        actionLog.add(record);
    }
    public void logInvalidAction(InvalidActionRecord record) {
        invalidActionLog.add(record);
    }
    public String getId() { return id; }
    public List<Player> getPlayers() { return players; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public ActionType getDeclaredAction() { return declaredAction; }
    public String getActingPlayerId() { return actingPlayerId; }
    public String getTargetId() { return targetId; }

    public String getBlockerId() { return blockerId; }
    public void setBlockerId(String id) { this.blockerId = id; }

    public CardType getBlockingRole() { return blockingRole; }
    public void setBlockingRole(CardType role) { this.blockingRole = role; }

    public String getChallengerId() { return challengerId; }
    public void setChallengerId(String id) { this.challengerId = id; }

    public String getWinnerId(Game game) {
        return game.getPlayers()
                .stream()
                .filter(Player::isAlive)
                .map(Player::getId)
                .findFirst()
                .orElse(null);
    }

    public void addPlayer(Player player) {
        if (state != GameState.WAITING_FOR_PLAYERS)
            throw new IllegalStateException("Game already started");
        players.add(player);
    }

    public void startGame() {
        initializeDeck();
        dealCards();
        logGameMemory("Turn " + turn);
        state = GameState.IN_PROGRESS;
    }

    private void initializeDeck() {
        List<CardType> cards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            cards.add(CardType.DUKE);
            cards.add(CardType.ASSASSIN);
            cards.add(CardType.CAPTAIN);
            cards.add(CardType.AMBASSADOR);
            cards.add(CardType.CONTESSA);
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
        this.turn++;
        logGameMemory("Turn " + turn + ":");
    }

    public CardType drawCard() { return deck.pop(); }

    public Player getPlayer(String id) {
        return players.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public void switchCard(String playerID, CardType cardToRemove) {
        logGameMemory(playerID + " switches a card" );
        this.deck.add(getPlayer(playerID).switchCard(deck.pop(),cardToRemove));

    }

    public void removeCard(String playerID, CardType cardToRemove) {
        logGameMemory(playerID + " looses a card" );
        this.deck.add(getPlayer(playerID).removeCard(cardToRemove));
        if(!getPlayer(playerID).isAlive()) {
            logGameMemory(playerID + " is dead");
        }

    }

    public void clearChallengeData(){
        this.challengerId = null;
        this.blockerId = null;
        this.blockingRole = null;
    }

    public void declareAction(ActionRecord actionRecord) {
        this.actingPlayerId = actionRecord.getPlayerId();
        this.declaredAction = actionRecord.getAction();

        switch(actionRecord.getAction()) {
            case STEAL -> this.getPlayer(actingPlayerId).incrementStealAttempts();
            case ASSASSINATE -> this.getPlayer(actingPlayerId).incrementAssassinationAttempts();
        }


        this.targetId = actionRecord.getTargetId();
        this.state = GameState.ACTION_DECLARED;
        this.actionLog.add(actionRecord);
        if(actionRecord.getTargetId() == null){
           logGameMemory(actionRecord.getPlayerId() + " calls " + actionRecord.getAction());
        } else{
            logGameMemory(actionRecord.getPlayerId() + " calls " + actionRecord.getAction() + " on " + actionRecord.getTargetId());
        }

    }


}