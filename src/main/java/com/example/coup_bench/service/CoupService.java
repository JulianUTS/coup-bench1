package com.example.coup_bench.service;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiAction;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.Scenario;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.repoModels.*;
import com.example.coup_bench.repo.GameRepository;
import com.example.coup_bench.repo.PlayerRepository;
import com.example.coup_bench.util.ActionUtil;
import com.example.coup_bench.util.PlayerUtil;
import com.example.coup_bench.util.RepoUtil;
import com.example.coup_bench.util.StatsUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CoupService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final ChallengeService challengeService;
    private final ActionService actionService;
    private final DeckService deck;
    private final HumanDecisionService human;

    public CoupService(GameRepository repo, PlayerRepository playerRepo,
                       ChallengeService challengeService,
                       ActionService actionService,
                       DeckService deckService,
                       HumanDecisionService humanDecisionService) {
        this.gameRepo = repo;
        this.playerRepo = playerRepo;
        this.challengeService = challengeService;
        this.actionService = actionService;
        this.deck = deckService;
        this.human = humanDecisionService;
    }

    public Game createGame(long seed) {
        return new Game(UUID.randomUUID().toString(), seed);
    }

    public Game joinGame(Game game, String playerId, String personality) {
        game.addPlayer(new Player(playerId, personality));
        return game;
    }

    public void startGame(Game game) {
        game.startGame();
        deck.initializeDeck();
        deck.dealCards(game.getPlayers());
        game.setState(GameState.WAITING_FOR_ACTION);
    }

    public void getAction(Game game) {
        Player currentPlayer = game.getCurrentPlayer();

        if(currentPlayer.isHuman()){
            human.printGetActionPrompt(game, currentPlayer);
            game.setState(GameState.WAITING_FOR_HUMAN_ACTION);
            return;
        }
        AiAction action= actionService.getAction(game, currentPlayer);
        if(game.getState() != GameState.DECLARE_ACTION) {
            return;
        }

        declareAction(game, game.getCurrentPlayer().getId(), action.action, actionService.getTargetId(), action.reason);
        challengeService.setChallengeScenario(ActionUtil.decideChallengeScenario(actionService.getDeclaredAction()));
        return;
    }

    public void resolveBlock(Game game) {
        challengeService.resolveBlock(game, actionService.getActionRecord());
    }

    public void getChallenge(Game game) {
        Scenario scenario = challengeService.getChallengeScenario();
        switch (scenario) {
            case S1       -> challengeService.applyS_1(game, actionService.getActionRecord());
            case S2_1     -> challengeService.applyS_2_1(game, actionService.getActionRecord());
            case S3_1     -> challengeService.applyS_3_1(game, actionService.getActionRecord());
            case S3_2     -> challengeService.applyS_3_2(game, actionService.getActionRecord());
            case S4_1     -> challengeService.applyS_4_1(game, actionService.getActionRecord());
            case S4_2     -> challengeService.applyS_4_2(game, actionService.getActionRecord());
            case NO_CHALLENGE -> game.setState(GameState.APPLY_ACTION);
        };

    }

    public void endGame(Game game) {
        GameSummary gamesummary = RepoUtil.getGameSummary(game);
        gameRepo.save(gamesummary);
        for (Player p : game.getPlayers()) {
            playerRepo.save(RepoUtil.getAgentLifetimeStats(p, playerRepo, gamesummary));
        }
        game.setState(GameState.FINISHED);
    }

    public void invalidGame(Game game) {
        game.logGameMemory("3 Invalid Actions used in a row, game is invalid");
        game.setState(GameState.ENDGAME);
    }

    public void declareAction(Game game, String playerId, ActionType action, String targetId, String reason) {
        this.actionService.declareAction(game, challengeService, playerId, action, targetId, reason);
    }

    public void resolveChallenge(Game game) {
        ActionRecord challengedRecord;
        if(challengeService.challengeOnBlock()){
            challengedRecord = challengeService.getBlockRecord();
        } else {
            challengedRecord = actionService.getActionRecord();
        }
        challengeService.resolveChallenge(game, deck, challengedRecord);
    }


    public void applyAction(Game game) {
        actionService.applyAction(game, deck);
        game.setState(GameState.NEXT_TURN);
    }

    public void nextTurn(Game game){
        if(!PlayerUtil.moreThanOnePlayer(game.getPlayers())){
            game.setState(GameState.ENDGAME);
            return;
        }

        challengeService.clearChallengeService();
        actionService.clearActionService();
        StatsUtil.logTurnSnapshot(game, actionService.getActionRecord());
        game.nextCurrentPlayer();
        game.incrementTurn();
        game.setState(GameState.WAITING_FOR_ACTION);

        if (game.getState() != GameState.FINISHED) {
            game.setState(GameState.IN_PROGRESS);
        }
    };

    public void applyBlock(Game game){
        StatsUtil.logSuccessfulBlock(game, challengeService.getBlocker(game),
                actionService.getActingPlayer(game),
                challengeService.getBlockRecord(),
                actionService.getActionRecord());
        game.setState(GameState.NEXT_TURN);
    };

    public void applyChallenge(Game game){
        if(challengeService.challengeOnBlock()){
            game.setState(GameState.APPLY_ACTION);
        } else{
            game.setState(GameState.NEXT_TURN);
        }
    };

}