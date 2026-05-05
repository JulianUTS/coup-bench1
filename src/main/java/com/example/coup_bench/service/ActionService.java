package com.example.coup_bench.service;

import com.example.coup_bench.model.repoModels.InteractionRecord;
import com.example.coup_bench.util.ActionUtil;
import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.InvalidActionRecord;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.PlayerUtil;
import com.example.coup_bench.model.Enums.ActionType;
import org.springframework.stereotype.Service;


@Service
public class ActionService {
    private ActionRecord actionRecord;
    private int invalidAction = 0;

    public ActionService() {
    }



    public void declareInvalidAction(Game game, String playerId,
                                     ActionType action, String targetId, String reason) {

        InvalidActionRecord invalidActionRecord = new InvalidActionRecord(
                playerId,
                action,
                targetId,
                PlayerUtil.isPlayerBluffing(game.getPlayer(playerId), action),
                reason,
                ActionUtil.getInvalidReason(game, game.getPlayer(playerId), action, targetId));

        game.logInvalidAction(invalidActionRecord);
        incrementInvalidAction();
    }

    public void declareAction(Game game, String playerId, ActionType action, String targetId, String reason) {
        Player player = game.getPlayer(playerId);

        // Validate first
        if (!ActionUtil.actionIsValid(game, playerId, action, targetId)){
            declareInvalidAction(game, playerId, action, targetId, reason);
        }

        ActionRecord actionRecord = new ActionRecord(playerId, action, targetId,
                PlayerUtil.isPlayerBluffing(game.getPlayer(playerId), action),  reason);

        updateActionAnalytics(game, player, actionRecord);
        logNewAction(game, actionRecord);
        setCurrentAction(actionRecord);
        game.setState(GameState.ACTION_DECLARED);
    }

    public void applyAction(Game game, DeckService deckService) {

        ActionType action = actionRecord.getAction();
        Player player = game.getPlayer(actionRecord.getPlayerId());
        Player target = game.getPlayer(actionRecord.getTargetId());

        if (actionRecord.getActionIsBluff()) {
            player.incrementBluffsSuccessful();
        }
        switch (action) {
            case INCOME -> {
                player.addCoins(1);
                player.incrementIncomeCount();
                game.logGameMemory(player.getId() + " gains 1 coin (INCOME)");
            }

            case FOREIGN_AID -> {
                player.addCoins(2);
                game.logGameMemory(player.getId() + " gains 2 coins (FOREIGN AID)");
            }

            case TAX -> {
                player.incrementTaxCount();
                player.addCoins(3);
                game.logGameMemory(player.getId() + " gains 3 coins (DUKE TAX)");
            }

            case STEAL -> {
                player.incrementStealSuccesses();
                int stolen = Math.min(2, target.getCoins());
                target.removeCoins(stolen);
                player.addCoins(stolen);
                game.logGameMemory(player.getId() + " steals " + stolen + " coins from " + target.getId());
            }
            case ASSASSINATE -> {
                if (game.getPlayer(target.getId()).isAlive()) {
                    player.removeCoins(3);
                    player.incrementAssassinationSuccesses();
                    game.logGameMemory(player.getId() + " assassinates " + target.getId());
                    deckService.removeCard(game, target);
                }
            }
            case COUP -> {
                player.removeCoins(7);
                deckService.removeCard(game, target);
                player.incrementCoupsPerformed();
            }

            case EXCHANGE -> {
                deckService.exchangeCards(game, player);
            }
        }
        //Successful Targeted Action
        if (target != null) {
            game.getGameAnalyticsService().logInteraction(new InteractionRecord(player.getId(), target.getId(), action, true));
        }
    }

    public void updateActionAnalytics(Game game, Player player, ActionRecord actionRecord) {
        //Update bluff values
        if (actionRecord.getActionIsBluff()) {
            player.incrementBluffsAttempted();
            game.getGameAnalyticsService().logBluff(actionRecord);
        }

        //Update action values
        switch(actionRecord.getAction()) {
            case STEAL -> player.incrementStealAttempts();
            case ASSASSINATE -> player.incrementAssassinationAttempts();
        }
    }

    public void logNewAction(Game game, ActionRecord actionRecord) {
        game.logAction(actionRecord);

        if(actionRecord.getTargetId() == null){
            game.logGameMemory(actionRecord.getPlayerId() + " calls " + actionRecord.getAction());
        } else{
            game.logGameMemory(actionRecord.getPlayerId() + " calls " + actionRecord.getAction() + " on " + actionRecord.getTargetId());
        }
    }

    public void setCurrentAction(ActionRecord actionRecord) {
        this.invalidAction = 0;
        this.actionRecord = actionRecord;
    }
    public void incrementInvalidAction() {
        invalidAction++;
    }

    public String getActingPlayerId() {
        return this.actionRecord.getPlayerId();
    }
    public ActionRecord getActionRecord() {
        return this.actionRecord;
    }

}
