package com.example.coup_bench.model.repoModels;
import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.InvalidActionRecord;
import com.example.coup_bench.model.Player;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "invalid_game_summaries")
public class InvalidGameSummary {

    @Id
    private String id;
    private String gameId;
    private String trial;
    private long totalGameDurationSec;

    private int numberOfPlayers;
    private String winnerId;
    private Map<String, Integer> seatOrder;
    private long seed;
    private int totalTurns;
    private int totalActions;
    private int totalChallenges;
    private int totalBlocks;
    private int totalInvalidActions;

    private List<String> gameMemory;
    private List<InvalidActionRecord> invalidActions;
    private List<ActionRecord> actions;
    private List<ActionRecord> bluffLog;
    private List<ActionRecord> challengeLog;
    private List<InteractionRecord> interactions;
    private List<TurnSnapshot> turnSnapshots;
    private List<Integer> coinsPerTurn = new ArrayList<>();
    private List<Integer> bluffsPerTurn = new ArrayList<>();
    private List<Player>  players;


    public InvalidGameSummary() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getTotalGameDurationSec() {
        return totalGameDurationSec;
    }

    public void setTotalGameDurationSec(long totalGameDurationSec) {
        this.totalGameDurationSec = totalGameDurationSec;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public void setTotalTurns(int totalTurns) {
        this.totalTurns = totalTurns;
    }

    public int getTotalActions() {
        return totalActions;
    }

    public void setTotalActions(int totalActions) {
        this.totalActions = totalActions;
    }

    public int getTotalChallenges() {
        return totalChallenges;
    }

    public void setTotalChallenges(int totalChallenges) {
        this.totalChallenges = totalChallenges;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public int getTotalInvalidActions() {
        return totalInvalidActions;
    }

    public void setTotalInvalidActions(int totalInvalidActions) {
        this.totalInvalidActions = totalInvalidActions;
    }

    public List<String> getGameMemory() {
        return gameMemory;
    }

    public void setGameMemory(List<String> gameMemory) {
        this.gameMemory = gameMemory;
    }

    public List<InvalidActionRecord> getInvalidActions() {
        return invalidActions;
    }

    public void setInvalidActions(List<InvalidActionRecord> invalidActions) {
        this.invalidActions = invalidActions;
    }

    public List<ActionRecord> getActions() {
        return actions;
    }

    public void setActions(List<ActionRecord> actions) {
        this.actions = actions;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<ActionRecord> getBluffLog() {
        return bluffLog;
    }

    public void setBluffLog(List<ActionRecord> bluffLog) {
        this.bluffLog = bluffLog;
    }

    public List<InteractionRecord> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<InteractionRecord> interactions) {
        this.interactions = interactions;
    }

    public List<TurnSnapshot> getTurnSnapshots() {
        return turnSnapshots;
    }

    public void setTurnSnapshots(List<TurnSnapshot> snapshots) {
        this.turnSnapshots = snapshots;
    }
    public Map<String, Integer> getSeatOrder() {
        return seatOrder;
    }

    public void setSeatOrder(Map<String, Integer> seatOrder) {
        this.seatOrder = seatOrder;
    }
    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }


    public List<ActionRecord> getChallengeLog() {
        return challengeLog;
    }

    public void setChallengeLog(List<ActionRecord> challengeLog) {
        this.challengeLog = challengeLog;
    }

    public List<Integer> getCoinsPerTurn() {
        return coinsPerTurn;
    }
    public void setCoinsPerTurn(List<Integer> coinsPerTurn) {
        this.coinsPerTurn = coinsPerTurn;
    }

    public List<Integer> getBluffsPerTurn() {
        return bluffsPerTurn;
    }
    public void setBluffsPerTurn(List<Integer> bluffsPerTurn) {
        this.bluffsPerTurn = bluffsPerTurn;
    }

    public String getTrial() {
        return trial;
    }

    public void setTrial(String trial) {
        this.trial = trial;
    }
}