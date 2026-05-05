package com.example.coup_bench.controller;

import java.util.List;

public class SimulationRequest {
    private int games;
    private long seed;
    private List<PlayerConfig> players;

    public int getGames() {
        return games;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public List<PlayerConfig> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerConfig> players) {
        this.players = players;
    }
}

