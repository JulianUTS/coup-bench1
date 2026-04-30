package com.example.coup_bench;

import com.example.coup_bench.model.Game;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    public List<Game> simulate(@RequestBody SimulationRequest req) {

        List<Game> results = new ArrayList<>();

        for (int i = 0; i < req.getGames(); i++) {

            long seed = (req.getSeed() == 0)
                    ? System.currentTimeMillis()
                    : req.getSeed();

            Game game = coup.createGame(seed);

            for (PlayerConfig pc : req.getPlayers()) {
                game = coup.joinGame(game, pc.getProvider(), pc.getPersonality());
            }

            game = coup.startGame(game);
            game = runner.runGame(game);

            results.add(game);
            System.out.println("Game "+ i + " completed at: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            System.out.println(game);
        }

        return results;
    }
}

