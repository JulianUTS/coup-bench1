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
        return ai.getAction(game, player);
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
        logActionToMemory(game, playerId, action, targetId);
        game.logGameMemory(ActionUtil.getInvalidReason(game, game.getPlayer(playerId), action, targetId));
        incrementInvalidAction();
        if(invalidAction == 3){
            game.setState(GameState.INVALID);
        }
    }

    public void declareAction(Game game, ChallengeService challengeService,
                              String playerId, AiAction actionResponse) {
        Player player = game.getPlayer(playerId);
        ActionType action = actionResponse.action;
        String targetId = actionResponse.targetId;
        String reason = actionResponse.reason;

        // Validate first
        if (!ActionUtil.actionIsValid(game, playerId, action, targetId)){
            declareInvalidAction(game, playerId, action, targetId, reason);
            return;
        }

        ActionRecord actionRecord = new ActionRecord(playerId, action, targetId,
                PlayerUtil.isPlayerBluffing(game.getPlayer(playerId), action),  reason);


        logNewAction(game, actionRecord);
        setCurrentAction(actionRecord);
        StatsUtil.logDeclaredAction(game, player, actionRecord);
        challengeService.setChallengeScenario(ActionUtil.decideChallengeScenario(action));
        game.setState(GameState.WAITING_FOR_CHALLENGE);
    }

    public void applyAction(Game game, DeckService deckService) {

        ActionType action = actionRecord.getAction();
        Player player = game.getPlayer(actionRecord.getPlayerId());
        String targetId = actionRecord.getTargetId();
        boolean targetAlive = true;

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
                int stolen = Math.min(2, game.getPlayer(targetId).getCoins());
                game.getPlayer(targetId).removeCoins(stolen);
                player.addCoins(stolen);
            }
            case ASSASSINATE -> {
                if (game.getPlayer(game.getPlayer(targetId).getId()).isAlive()) {
                    player.removeCoins(3);
                }
            }
            case COUP -> {
                player.removeCoins(7);
            }
            case EXCHANGE -> {
                deckService.exchangePlayerCards(player);
            }
        }
        logAppliedAction(game, player.getId(), targetId, action);
        StatsUtil.logSuccessfulAction(game, player, actionRecord);

        switch (action) {
            case COUP, ASSASSINATE: {
                targetAlive = deckService.removePlayerCard(game, game.getPlayer(targetId));
            }
        }

        if(!targetAlive){
            StatsUtil.logPlayerKilled(player, game.getPlayer(targetId));
            game.logGameMemory(targetId + " has lost all their cards");
        }

        game.setState(GameState.NEXT_TURN);
    }
    public void logAppliedAction(Game game, String player, String target, ActionType action) {

        switch (action) {

            case INCOME -> game.logGameMemory(player + " gains 1 coin (INCOME)");

            case FOREIGN_AID -> game.logGameMemory(player + " gains 2 coins (FOREIGN AID)");

            case TAX -> game.logGameMemory(player + " gains 3 coins (DUKE TAX)");

            case STEAL -> game.logGameMemory(player + " steals coins from " + target + " (CAPTAIN STEAL)");

            case ASSASSINATE -> game.logGameMemory(player + " assassinates " + target + " (ASSASSIN ASSASSINATE)");

            case COUP -> game.logGameMemory(player + " coups " + target + " (COUP)");

            case EXCHANGE -> game.logGameMemory(player + " exchanges cards (AMBASSADOR EXCHANGE)");
        }
    }




    public void logNewAction(Game game, ActionRecord actionRecord) {
        game.logAction(actionRecord);
        logActionToMemory(game, actionRecord.getPlayerId(),actionRecord.getAction(), actionRecord.getTargetId());
    }
    public void logActionToMemory(Game game, String playerId, ActionType action,String targetId) {
        if(targetId == null){
            game.logGameMemory(playerId + " calls " + action);
        } else{
            game.logGameMemory(playerId + " calls " + action + " on " + targetId);
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
