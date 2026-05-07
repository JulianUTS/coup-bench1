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
import com.example.coup_bench.util.HumanUtil;
import com.example.coup_bench.util.RepoUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CoupService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final ChallengeService challengeService;
    private final ActionService actionService;
    private final DeckService deck;
    private final GameAnalyticsService stats;
    private final AiActionService ai;
    private final HumanDecisionService human;

    public CoupService(GameRepository repo, PlayerRepository playerRepo,
                       ChallengeService challengeService,
                       ActionService actionService, DeckService deckService,
                       GameAnalyticsService gameAnalyticsService,
                       AiActionService aiDecisionService, HumanDecisionService humanDecisionService) {
        this.gameRepo = repo;
        this.playerRepo = playerRepo;
        this.challengeService = challengeService;
        this.actionService = actionService;
        this.deck = deckService;
        this.stats = gameAnalyticsService;
        this.ai = aiDecisionService;
        this.human = humanDecisionService;
    }

    public Game createGame(long seed) {
        return new Game(UUID.randomUUID().toString(), seed);
    }

    public Game joinGame(Game game, String playerId, String personality) {
        game.addPlayer(new Player(playerId, personality));
        return game;
    }

    public Game startGame(Game game) {
        game.startGame();
        deck.initializeDeck();
        deck.dealCards(game.getPlayers());
        game.setState(GameState.WAITING_FOR_ACTION);
        return game;
    }

    public Game getAction(Game game) {
        Player currentPlayer = game.getCurrentPlayer();

        if(currentPlayer.isHuman()){
            human.printGetActionPrompt(game, currentPlayer);
            game.setState(GameState.WAITING_FOR_HUMAN_ACTION);
            return game;
        }
        AiAction action= actionService.getAction(game, currentPlayer);
        if(game.getState() != GameState.DECLARE_ACTION) {
            return game;
        }

        declareAction(game, game.getCurrentPlayer().getId(), action.action, actionService.getTargetId(), action.reason);
        return game;
    }

    public Game getChallenge(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        Scenario scenario = challengeService.getChallengeScenario();
        switch (scenario) {
            case S1       -> challengeService.applyS_1(game, actionService.getActionRecord());
            case S2_1     -> Scenario.S2_1;
            case S2_2     -> Scenario.S2_2;
            case S3_1     -> Scenario.S3_1;
            case S3_2     -> Scenario.S3_2;
            case S3_3     -> Scenario.S3_3;
            case S4_1     -> Scenario.S4_1;
            case S4_2     -> Scenario.S4_2;
            case S4_3     -> Scenario.S4_3;
            case NO_CHALLENGE -> game.setState(GameState.APPLY_ACTION);
        };

        return game;
    }

    public Game saveIfFinished(Game game) {
        if (game.getState() == GameState.FINISHED || game.getState() == GameState.INVALID) {
            GameSummary gamesummary = RepoUtil.getGameSummary(game);
            gameRepo.save(gamesummary);
            for(Player p : game.getPlayers()){
                playerRepo.save(RepoUtil.getAgentLifetimeStats(p, playerRepo, gamesummary));
            }
        }
        return game;
    }

    public Game invalidGame(Game game) {
        game.logGameMemory("3 Invalid Actions used in a row, game is invalid");
        game.setState(GameState.ENDGAME);
        return saveIfFinished(game);
    }



    public void declareAction(Game game, String playerId, ActionType action, String targetId, String reason) {
        this.actionService.declareAction(game, stats, challengeService, playerId, action, targetId, reason);
    }

    public Game logAction(Game game, ActionRecord actionRecord) {
        game.logAction(actionRecord);
         return game;
    }

    public Game declareBlock(Game game, String blockerId, AiReaction reaction) {
        challengeService.declareBlock(game, stats, blockerId, reaction.action,
                actionService.getActingPlayerId() ,reaction.reason);
        return game;
    }

    public Game declareChallenge(Game game, String challengerId , AiReaction aiReaction) {
        String challengedId;
        //Determine target
        if(challengeService.challengeOnBlock()){
            challengedId = challengeService.getBlockRecord().getPlayerId();
        } else {
            challengedId = actionService.getActingPlayerId();
        }
        challengeService.declareChallenge(game, stats, challengerId, challengedId, aiReaction.reason);
        return game;
    }

    public Game resolveChallenge(Game game) {
        ActionRecord challengedRecord;
        if(challengeService.challengeOnBlock()){
            challengedRecord = challengeService.getBlockRecord();
        } else {
            challengedRecord = actionService.getActionRecord();
        }
        challengeService.resolveChallenge(game, stats, deck, challengedRecord);
        return game;
    }


    public Game applyAction(Game game) {
        actionService.applyAction(game, deck, stats);
        game.setState(GameState.NEXT_TURN);
        return game;
    }

    public Game nextTurn(Game game){
        challengeService.clearChallengeService();
        actionService.clearActionService();
        stats.logTurnSnapshot(game, actionService.getActionRecord());
        game.setState(GameState.WAITING_FOR_ACTION);



        game.nextTurn();
        if (game.getState() != GameState.FINISHED) {
            game.setState(GameState.IN_PROGRESS);
        }
        return saveIfFinished(game);
    };

    public Game applyBlock(Game game){
        stats.logSuccessfulBlock(game, challengeService.getBlocker(game),
                actionService.getActingPlayer(game),
                challengeService.getBlockRecord(),
                actionService.getActionRecord());
        game.setState(GameState.NEXT_TURN);
        return game;
    };

    public Game applyChallenge(Game game){
        if(challengeService.challengeOnBlock()){
            game.setState(GameState.APPLY_ACTION);
        } else{
            game.setState(GameState.NEXT_TURN);
        }
        return game;
    };





}