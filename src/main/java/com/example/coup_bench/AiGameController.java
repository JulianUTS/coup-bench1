package com.example.coup_bench;

import com.example.coup_bench.model.Game;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiGameController {

    private final CoupService coup;
    private final AiGameRunner runner;

    public AiGameController(CoupService coup, AiGameRunner runner) {
        this.coup = coup;
        this.runner = runner;
    }

    @PostMapping("/simulate")
    public Game simulate() {

        Game game = coup.createGame();

        coup.joinGame(game.getId(), "p1", "Alice", "openai");
        coup.joinGame(game.getId(), "p2", "Bob", "claude");

        coup.startGame(game.getId());

        return runner.runGame(game.getId());
    }
}

