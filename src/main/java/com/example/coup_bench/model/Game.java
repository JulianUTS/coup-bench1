package com.example.coup_bench.model;

import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.service.DeckService;
import com.example.coup_bench.service.GameAnalyticsService;

import java.util.*;

public class Game {

    private final String id;
    private final List<Player> players = new ArrayList<>();
    private final List<ActionRecord> actionLog = new ArrayList<>();
    private final List<String> gameMemory = new ArrayList<>();
    private final List<InvalidActionRecord> invalidActionLog = new ArrayList<>();
    private final long timestampStart = System.currentTimeMillis();
    private final GameAnalyticsService gameAnalyticsService;

    private final long seed;

    private int currentPlayerIndex = 0;
    private GameState state = GameState.WAITING_FOR_PLAYERS;
    private int turn = 1;
    private int TotalBlocks = 0;
    private int TotalChallenges = 0;




    public Game(String id, long seed, GameAnalyticsService gameAnalyticsService) {
        this.id = id;
        this.seed = seed;
        this.gameAnalyticsService = gameAnalyticsService;
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
   ;

    public Map<String, Integer> seatOrder;


    public int getInvalidAction() {
        return invalidActionLog.size();
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
        Random rng = new Random(seed);
        Collections.shuffle(players, rng);

        this.seatOrder = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            seatOrder.put(players.get(i).getId(), i);
        }

        logGameMemory("Turn " + turn);
    }



    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        for (Player player:  players){
            if(player.isAlive()){
                player.incrementTurnsSurvived();
            }
        }
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

    public Player getPlayer(String id) {
        return players.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }


    public long getSeed() { return seed; }

    public Map<String, Integer> getSeatOrder() { return seatOrder; }

    public GameAnalyticsService getGameAnalyticsService() {
        return gameAnalyticsService;
    }
}