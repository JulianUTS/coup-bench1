package com.example.coup_bench;

import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.repo.GameRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/test")
public class TestDbController {

    private final GameRepository repo;

    public TestDbController(GameRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/game")
    public Game createTestGame() {

        Game game = new Game("test-" + UUID.randomUUID());

        // Add a dummy player
        Player p = new Player("p1", "Test Player", "openai");
        game.addPlayer(p);

        // Save to MongoDB
        return repo.save(game);
    }
}

