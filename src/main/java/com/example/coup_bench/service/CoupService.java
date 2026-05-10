package com.example.coup_bench.service;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiAction;
import com.example.coup_bench.model.Enums.Scenario;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.repoModels.*;
import com.example.coup_bench.repo.GameRepository;
import com.example.coup_bench.repo.PlayerRepository;
import com.example.coup_bench.util.HumanUtil;
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
    private final HumanService human;

    public CoupService(GameRepository repo, PlayerRepository playerRepo,
                       ChallengeService challengeService,
                       ActionService actionService,
                       DeckService deckService,
                       HumanService humanDecisionService) {
        this.gameRepo = repo;
        this.playerRepo = playerRepo;
        this.challengeService = challengeService;
        this.actionService = actionService;
        this.deck = deckService;
        this.human = humanDecisionService;
    }


    public void getHumanChooseCard(Game game, HumanChooseCardRequest req) {
        deck.removeHumanCard(game, req.getCard());
        game.setState(human.getPrevious());
        human.setPrevious(null);


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
            HumanUtil.printGetActionPrompt(game, currentPlayer);
            game.setState(GameState.WAITING_FOR_HUMAN_ACTION);
            return;
        }
        AiAction action= actionService.getAction(game, currentPlayer);

        declareAction(game, game.getCurrentPlayer().getId(), action);

    }
    public void getHumanAction(Game game, HumanActionRequest humanAction){
        AiAction action = new AiAction();
        action.action = humanAction.getAction();
        action.targetId = humanAction.getTargetId();
        declareAction(game, game.getCurrentPlayer().getId(), action);
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
        game.logGameMemory(game.getWinnerId(game) + " wins!!!");
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

    public void declareAction(Game game, String playerId, AiAction action) {
        this.actionService.declareAction(game, challengeService, playerId, action);
    }

    public void resolveChallenge(Game game) {
        ActionRecord challengedRecord;
        if(challengeService.challengeOnBlock()){
            challengedRecord = challengeService.getBlockRecord();
        } else {
            challengedRecord = actionService.getActionRecord();
        }
        challengeService.resolveChallenge(game, deck, challengedRecord, human);
    }


    public void applyAction(Game game) {
        actionService.applyAction(game, deck, human);
    }

    public void nextTurn(Game game){
        if(PlayerUtil.onlyOneLeft(game.getPlayers())){
            game.setState(GameState.ENDGAME);
            return;
        }
        StatsUtil.logTurnSnapshot(game, actionService.getActionRecord());
        challengeService.clearChallengeService();
        actionService.clearActionService();

        game.nextCurrentPlayer();
        game.incrementTurn();
        StatsUtil.incrementTurnsSurvived(game.getPlayers());
        game.setState(GameState.WAITING_FOR_ACTION);
    };

    public ChallengeService getChallengeService() {
        return challengeService;
    }
}