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
        game = coup.joinGame(game, "openai", "aggressive");
        game = coup.joinGame(game, "claude", "defensive");
        game = coup.joinGame(game, "gemini", "aggressive");

        game = coup.startGame(game);

        return runner.runGame(game);
    }
}

