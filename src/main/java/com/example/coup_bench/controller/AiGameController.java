package com.example.coup_bench.controller;

import com.example.coup_bench.AiGameRunner;
import com.example.coup_bench.model.*;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.humanResponses.HumanActionRequest;
import com.example.coup_bench.model.humanResponses.HumanChooseCardRequest;
import com.example.coup_bench.model.humanResponses.HumanExchangeCardRequest;
import com.example.coup_bench.model.humanResponses.HumanReactionRequest;
import com.example.coup_bench.repo.CurrentGameRepository;
import com.example.coup_bench.repo.InvalidGameRepository;
import com.example.coup_bench.service.CoupService;
import com.example.coup_bench.util.RepoUtil;
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
            "grok",
            "deepseek"
    );
    private static final List<String> PERSONALITIES = List.of(
            "aggressive",
            "defensive",
            "analytical",
            "chaotic",
            "default",
            "opportunistic"
    );





    public AiGameController(CoupService coup, AiGameRunner runner, CurrentGameRepository currentGame) {
        this.coup = coup;
        this.runner = runner;
        this.currentGame = currentGame;
    }

    @PostMapping("/simulate")
    public String simulate(@RequestBody SimulationRequests simulationRequests) {
        String current = "No trial completed";
        for (SimulationRequest req : simulationRequests.getSimulationRequests()) {
            System.out.println("Trial " + req.getTrial() + " starting:");
            int i = req.getGamesCompleted();
            while (i < req.getGames()) {
                long seed = (req.getSeed() == 0)
                        ? System.currentTimeMillis()
                        : req.getSeed();
                String trial = req.getTrial() + "-" + (i + 1);
                current = "Last started trial: " + trial;
                Game game = coup.createGame(seed, trial);
                System.out.println("Game " + trial + " created with seed " + seed);

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

                try {
                    runner.runGame(game);
                    if (game.getState() == GameState.INVALID) {
                        System.out.println("Restarting game " + i);
                    } else{
                        i++;
                    }
                } catch (Throwable t) {
                    System.err.println(t.getMessage());
                }
            }
            System.out.println("Trial " + req.getTrial() + " finished |");
        }
        return current;
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
    public String simulateHuman(@RequestBody SimulationRequest req) {
        long seed = (req.getSeed() == 0)
                ? System.currentTimeMillis()
                : req.getSeed();

        Game game = coup.createGame(seed, req.getTrial());
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
        return coup.getHuman().getCurrentPrompt();

    }

    @PostMapping("/human/action")
    public String humanAction(@RequestBody HumanActionRequest req) {
        Game game = currentGame.get();
        coup.getHumanAction(game, req);
        runner.runGame(game);
        currentGame.save(game);
        return coup.getHuman().getCurrentPrompt();
    }
    @PostMapping("/human/reaction")
    public String humanReaction(@RequestBody HumanReactionRequest req) {
        Game game = currentGame.get();
        coup.getHumanReaction(game, req);
        runner.runGame(game);
        currentGame.save(game);
        return coup.getHuman().getCurrentPrompt();
    }
    @PostMapping("/human/chooseCard")
    public String humanChooseCard(@RequestBody HumanChooseCardRequest req) {
        Game game = currentGame.get();
        coup.getHumanChooseCard(game, req);
        runner.runGame(game);
        currentGame.save(game);
        return coup.getHuman().getCurrentPrompt();
    }
    @PostMapping("/human/exchangeCards")
    public String humanExchangeCards(@RequestBody HumanExchangeCardRequest req) {
        Game game = currentGame.get();
        coup.getHumanExchangeCard(game, req);
        runner.runGame(game);
        currentGame.save(game);
        return coup.getHuman().getCurrentPrompt();
    }



}

