package com.example.coup_bench.model.repoModels;
import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.InvalidActionRecord;
import com.example.coup_bench.model.Player;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "game_summaries")
public class GameSummary {

    @Id
    private String id;
    private String gameId;
    private long timestampStart;
    private long timestampEnd;

    private int numberOfPlayers;
    private String winnerId;
    private int totalTurns;
    private int totalActions;
    private int totalChallenges;
    private int totalBlocks;
    private int totalInvalidActions;

    private List<String> gameMemory;
    private List<InvalidActionRecord> invalidActions;
    private List<ActionRecord> actions;
    private List<Player>  players;


    public GameSummary() {}

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

    public long getTimestampStart() {
        return timestampStart;
    }

    public void setTimestampStart(long timestampStart) {
        this.timestampStart = timestampStart;
    }

    public long getTimestampEnd() {
        return timestampEnd;
    }

    public void setTimestampEnd(long timestampEnd) {
        this.timestampEnd = timestampEnd;
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

}