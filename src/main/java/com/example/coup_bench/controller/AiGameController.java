package com.example.coup_bench.controller;

import com.example.coup_bench.AiGameRunner;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.repo.CurrentGameRepository;
import com.example.coup_bench.service.CoupService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/ai")
public class AiGameController {

    private final CoupService coup;
    private final AiGameRunner runner;
    private final CurrentGameRepository currentGame;
    private static final List<String> PROVIDERS = List.of(
            "openai",
            "claude",
            "gemini",
            "grok"
    );
    private static final List<String> PERSONALITIES = List.of(
            "aggressive",
            "defensive",
            "analytical",
            "chaotic",
            "default",
            "opportunistic"
    );





    public AiGameController(CoupService coup, AiGameRunner runner,  CurrentGameRepository currentGame) {
        this.coup = coup;
        this.runner = runner;
        this.currentGame = currentGame;
    }

    @PostMapping("/simulate")
    public List<Game> simulate(@RequestBody SimulationRequest req) {

        List<Game> results = new ArrayList<>();

        for (int i = 0; i < req.getGames(); i++) {

            long seed = (req.getSeed() == 0)
                    ? System.currentTimeMillis()
                    : req.getSeed();

            Game game = coup.createGame(seed);
            System.out.println("Game "+ i + " created with seed " + seed);

            // Track which providers are already used
            Set<String> usedProviders = new HashSet<>();

            for (PlayerConfig pc : req.getPlayers()) {

                // -----------------------------
                // 1. Resolve provider
                // -----------------------------
                String provider = pc.getProvider();

                if (provider.equalsIgnoreCase("random")) {
                    provider = pickRandomProvider(usedProviders);
                }

                usedProviders.add(provider);

                // -----------------------------
                // 2. Resolve personality
                // -----------------------------
                String personality = pc.getPersonality();

                if (personality.equalsIgnoreCase("random")) {
                    personality = pickRandomPersonality();
                }

                // -----------------------------
                // 3. Join player
                // -----------------------------

                game = coup.joinGame(game, provider, personality);
                System.out.println("Player " + provider + " joined with " + personality + " personality");
            }

            coup.startGame(game);
            System.out.println("Game "+ i + " started at: " +
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            runner.runGame(game);

            results.add(game);
            System.out.println("Game "+ i + " completed at: " +
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }

        return results;
    }
    private String pickRandomProvider(Set<String> used) {
        List<String> available = PROVIDERS.stream()
                .filter(p -> !used.contains(p))
                .toList();

        if (available.isEmpty()) {
            throw new IllegalStateException("No providers left to assign");
        }

        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    private String pickRandomPersonality() {
        return PERSONALITIES.get(
                ThreadLocalRandom.current().nextInt(PERSONALITIES.size())
        );
    }
    @PostMapping("/simulateHuman")
    public Game simulateHuman(@RequestBody SimulationRequest req) {
        long seed = (req.getSeed() == 0)
                ? System.currentTimeMillis()
                : req.getSeed();

        Game game = coup.createGame(seed);
        System.out.println("Game with human created with seed " + seed);

        // Track which providers are already used
        Set<String> usedProviders = new HashSet<>();
        for (PlayerConfig pc : req.getPlayers()) {

            // -----------------------------
            // 1. Resolve provider
            // -----------------------------
            String provider = pc.getProvider();

            if (provider.equalsIgnoreCase("random")) {
                provider = pickRandomProvider(usedProviders);
            }

            usedProviders.add(provider);

            // -----------------------------
            // 2. Resolve personality
            // -----------------------------
            String personality = pc.getPersonality();

            if (personality.equalsIgnoreCase("random")) {
                personality = pickRandomPersonality();
            }

            // -----------------------------
            // 3. Join player
            // -----------------------------

            game = coup.joinGame(game, provider, personality);
            System.out.println("Player " + provider + " joined with " + personality + " personality");
        }
        coup.startGame(game);
        System.out.println("Game with human started at: " +
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        runner.runGame(game);
        currentGame.save(game);
        return game;

    }

    @PostMapping("/human/action")
    public Game humanAction(@RequestBody HumanActionRequest req) {
        Game game = currentGame.get();
        game = runner.applyHumanAction(game, req);
        runner.runUntilHuman(game);
        currentGame.save(game);
        return game;
    }
    @PostMapping("/human/reaction")
    public Game humanReaction(@RequestBody HumanReactionRequest req) {
        Game game = currentGame.get();
        game = runner.applyHumanReaction(game, req);
        runner.runUntilHuman(game);
        currentGame.save(game);
        return game;
    }
    @PostMapping("/human/chooseCard")
    public Game humanChooseCard(@RequestBody HumanChooseCardRequest req) {
        Game game = currentGame.get();
        game = runner.applyHumanChooseCard(game, req);
        runner.runUntilHuman(game);
        currentGame.save(game);
        return game;
    }



}

