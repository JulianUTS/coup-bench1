package com.example.coup_bench.service;

import com.example.coup_bench.model.AiResponses.AiAction;
import com.example.coup_bench.util.ActionUtil;
import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.InvalidActionRecord;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.PlayerUtil;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.util.StatsUtil;
import org.springframework.stereotype.Service;


@Service
public class ActionService {
    private final AiActionService ai;

    private ActionRecord actionRecord;
    private int invalidAction = 0;

    public ActionService(AiActionService aiAction) {
        this.ai = aiAction;
    }

    public AiAction getAction(Game game, Player player) {
        AiAction action = ai.getAction(game, player);
        game.setState(GameState.DECLARE_ACTION);
        return action;
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
        if(invalidAction == 3){
            game.setState(GameState.INVALID);
        }
    }

    public void declareAction(Game game, ChallengeService challengeService,
                              String playerId, ActionType action, String targetId, String reason) {
        Player player = game.getPlayer(playerId);

        // Validate first
        if (!ActionUtil.actionIsValid(game, playerId, action, targetId)){
            declareInvalidAction(game, playerId, action, targetId, reason);
            return;
        }

        ActionRecord actionRecord = new ActionRecord(playerId, action, targetId,
                PlayerUtil.isPlayerBluffing(game.getPlayer(playerId), action),  reason);

        StatsUtil.logDeclaredAction(game, player, actionRecord);
        logNewAction(game, actionRecord);
        setCurrentAction(actionRecord);
        challengeService.setChallengeScenario(ActionUtil.decideChallengeScenario(action));
        game.setState(GameState.WAITING_FOR_CHALLENGE);
    }

    public void applyAction(Game game, DeckService deckService) {

        ActionType action = actionRecord.getAction();
        Player player = game.getPlayer(actionRecord.getPlayerId());
        Player target = game.getPlayer(actionRecord.getTargetId());

        switch (action) {
            case INCOME -> {
                player.addCoins(1);
            }

            case FOREIGN_AID -> {
                player.addCoins(2);
            }

            case TAX -> {
                player.addCoins(3);
            }

            case STEAL -> {
                int stolen = Math.min(2, target.getCoins());
                target.removeCoins(stolen);
                player.addCoins(stolen);
            }
            case ASSASSINATE -> {
                if (game.getPlayer(target.getId()).isAlive()) {
                    player.removeCoins(3);
                    deckService.removePlayerCard(game, target);
                }
            }
            case COUP -> {
                player.removeCoins(7);
                deckService.removePlayerCard(game, target);
            }
            case EXCHANGE -> {
                deckService.exchangePlayerCards(player);
            }
        }
        logAppliedAction(game, player, target, action);
        game.setState(GameState.NEXT_TURN);
    }
    public void logAppliedAction(Game game, Player player, Player target, ActionType action) {

        switch (action) {

            case INCOME -> game.logGameMemory(player.getId() + " gains 1 coin (INCOME)");

            case FOREIGN_AID -> game.logGameMemory(player.getId() + " gains 2 coins (FOREIGN AID)");

            case TAX -> game.logGameMemory(player.getId() + " gains 3 coins (DUKE TAX)");

            case STEAL -> game.logGameMemory(player.getId() + " steals coins from " + target.getId() + " (CAPTAIN STEAL)");

            case ASSASSINATE -> game.logGameMemory(player.getId() + " assassinates " + target.getId() + " (ASSASSIN ASSASSINATE)");

            case COUP -> game.logGameMemory(player.getId() + " coups " + target.getId() + "COUP");

            case EXCHANGE -> game.logGameMemory(player.getId() + " exchanges cards (AMBASSADOR EXCHANGE)");
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
        this.actionRecord = actionRecord;
    }

    public void clearActionService(){
        this.invalidAction = 0;
        this.actionRecord = null;
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

    public String getTargetId(){
        return this.actionRecord.getTargetId();
    }
    public ActionType getDeclaredAction() {
        return this.actionRecord.getAction();
    }
    public Boolean actionIsBluff() {
        return this.actionRecord.getActionIsBluff();
    }

    public Player getActingPlayer(Game game){
        return game.getPlayer(this.actionRecord.getPlayerId());
    }

    public boolean targetedAction(){
        return  this.actionRecord.getTargetId() != null;
    }

}
