package com.example.coup_bench;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.AiResponses.AiAction;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.service.AiActionService;
import com.example.coup_bench.service.CoupService;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;


@Service
public class AiGameRunner {

    private final CoupService coup;



    public AiGameRunner(CoupService coup) {
        this.coup = coup;
    }

    public void runGame(Game game) {

        while(!gameFinished(game) && !gameWaitingForHuman(game)) {
            System.out.println(game.getState().toString());
            nextMove(game);
        }
    }
    public void nextMove(Game game){
        GameState gameState = game.getState();
        switch (gameState) {
            case WAITING_FOR_ACTION -> coup.getAction(game);
            case WAITING_FOR_CHALLENGE -> coup.getChallenge(game);
            case APPLY_ACTION -> coup.applyAction(game);
            case CHALLENGE_DECLARED -> coup.resolveChallenge(game);
            case BLOCK_DECLARED -> coup.resolveBlock(game);
            case INVALID -> coup.invalidGame(game);
            case NEXT_TURN -> coup.nextTurn(game);
            case ENDGAME -> coup.endGame(game);

        }
    }

    public boolean gameFinished(Game game) {
        return game.getState() == GameState.FINISHED;
    }
    public boolean gameWaitingForHuman(Game game) {
        return game.getState() == GameState.WAITING_FOR_HUMAN_ACTION;
    }

}


