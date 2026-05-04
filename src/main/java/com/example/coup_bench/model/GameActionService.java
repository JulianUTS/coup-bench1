package com.example.coup_bench.model;

import com.example.coup_bench.RoleUtil;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Enums.GameState;
import org.springframework.stereotype.Service;


@Service
public class GameActionService {

    public GameActionService() {
    }

    public void declareBlock(Game game, ChallengeService challengeService, String blockerId,
                             ActionType blockAction, String blockReason) {

        ActionRecord blockRecord = new ActionRecord(
                blockerId,
                blockAction,
                game.getActingPlayerId(),
                PlayerUtil.isPlayerBluffing(game.getPlayer(blockerId), blockAction),
                blockReason);

        challengeService.blockDeclared(blockerId, RoleUtil.getCard(blockAction));
        game.blockDeclared(blockRecord);
    }
}
