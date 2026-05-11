package com.example.coup_bench.repo;

import com.example.coup_bench.model.Game;
import org.springframework.stereotype.Repository;

@Repository
public class CurrentGameRepository {

    private Game currentGame;

    public synchronized void save(Game game) {
        this.currentGame = game;
    }

    public synchronized Game get() {
        return currentGame;
    }

    public synchronized boolean exists() {
        return currentGame != null;
    }

    public synchronized void clear() {
        this.currentGame = null;
    }
}
