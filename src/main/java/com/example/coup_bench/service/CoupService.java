package com.example.coup_bench.service;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.repoModels.*;
import com.example.coup_bench.repo.GameRepository;
import com.example.coup_bench.repo.PlayerRepository;
import com.example.coup_bench.util.RepoUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CoupService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final ChallengeService challengeService;
    private final ActionService actionService;
    private final DeckService deckService;
    private final GameAnalyticsService gameAnalyticsService;

    public CoupService(GameRepository repo, PlayerRepository playerRepo,
                       ChallengeService challengeService,
                       ActionService actionService,  DeckService deckService,
                       GameAnalyticsService gameAnalyticsService) {
        this.gameRepo = repo;
        this.playerRepo = playerRepo;
        this.challengeService = challengeService;
        this.actionService = actionService;
        this.deckService = deckService;
        this.gameAnalyticsService = gameAnalyticsService;

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
        deckService.initializeDeck();
        deckService.dealCards(game.getPlayers());
        game.setState(GameState.DECIDING_ACTION);
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
        game.setState(GameState.INVALID);
        return saveIfFinished(game);
    }



    public Game declareAction(Game game, String playerId, ActionType action, String targetId, String reason) {
        actionService.declareAction(game, playerId, action, targetId, reason);
        return game;
    }

    public Game logAction(Game game, ActionRecord actionRecord) {
        game.logAction(actionRecord);
         return game;
    }

    public Game declareBlock(Game game, String blockerId, AiReaction reaction) {
        challengeService.declareBlock(game, blockerId, reaction.action,
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
        challengeService.declareChallenge(game, challengerId, challengedId, aiReaction.reason);
        return game;
    }


    public CardType chooseCard(Game game, Player player, AiDecisionService ai) {
        if (player.getCards().size() == 1) {
            return player.getCards().getFirst();
        }
        return ai.getCardToLoose(game, player);

    }

    public Game resolveChallenge(Game game, AiDecisionService ai) {
        ActionRecord challengedRecord;
        if(challengeService.challengeOnBlock()){
            challengedRecord = challengeService.getBlockRecord();
        } else {
            challengedRecord = actionService.getActionRecord();
        }
        challengeService.resolveChallenge(game, challengedRecord);
        return game;
    }


    public Game applyAction(Game game) {
        actionService.applyAction(game, deckService);
        return game;
    }

    public Game nextTurn(Game game){
        challengeService.clearChallengeService();
        actionService.clearActionService();
        TurnSnapshot snap = new TurnSnapshot(
                game.getTurn(),
                game.getPlayers().stream()
                        .collect(Collectors.toMap(Player::getId, Player::getCoins)),
                game.getPlayers().stream()
                        .collect(Collectors.toMap(Player::getId, p -> p.getCards().size())),
                game.getDeclaredAction(),
                game.getActingPlayerId(),
                game.getTargetId()
        );
        game.getGameAnalyticsService().logTurnSnapshot(snap);



        game.nextTurn();
        if (game.getState() != GameState.FINISHED) {
            game.setState(GameState.IN_PROGRESS);
        }
        return saveIfFinished(game);
    };

    public Game applyBlock(Game game){
        gameAnalyticsService.logSuccessfulBlock(game.get);
        challengeService.getBlocker(game).incrementBlocksSuccessful();

        //Unsuccessful targeted action
        if(actionService.targetedAction()) {
            game.getGameAnalyticsService().logInteraction(new InteractionRecord(
                    actionService.getActingPlayerId(),
                    actionService.getTargetId(),
                    actionService.getDeclaredAction(), false));

        }

        //Successful Block
        game.getGameAnalyticsService().logInteraction(new InteractionRecord(
                challengeService.getBlockerId(),
                actionService.getActingPlayerId(),
                challengeService.getBlockAction(), true));



        if(challengeService.blockIsBluff()){
            challengeService.getBlocker(game).incrementBluffsSuccessful();
        }

        if(actionService.actionIsBluff()){
            actionService.getActingPlayer(game).incrementBluffsFailed();
        }

        return game;
    };

    public Game applyChallenge(Game game){

        return game;
    };





}