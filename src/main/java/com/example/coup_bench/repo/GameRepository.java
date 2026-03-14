package com.example.coup_bench.repo;


import com.example.coup_bench.model.Game;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepository {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public void save(Game game) {
        games.put(game.getId(), game);
    }

    public Game find(String id) {
        Game game = games.get(id);
        if (game == null) throw new IllegalArgumentException("Game not found");
        return game;
    }
}
