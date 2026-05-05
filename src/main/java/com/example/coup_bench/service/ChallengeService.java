package com.example.coup_bench.service;

import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.model.repoModels.InteractionRecord;
import com.example.coup_bench.util.PlayerUtil;
import com.example.coup_bench.util.RoleUtil;
import org.springframework.stereotype.Service;


@Service
public class ChallengeService {
    private ActionRecord blockRecord;
    private String challengerId;
    private Player challengeWinner;
    private Player challengeLoser;

    public void declareBlock(Game game, String blockerId,
                             ActionType blockAction, String blockedId, String blockReason) {

        Player blocker = game.getPlayer(blockerId);
        ActionRecord blockRecord = new ActionRecord(
                blockerId,
                blockAction,
                blockedId,
                PlayerUtil.isPlayerBluffing(game.getPlayer(blockerId), blockAction),
                blockReason);

        updateBlockAnalytics(game, blocker, blockRecord);
        logNewBlock(game, blockRecord);
        setCurrentBlock(blockRecord);
        game.setState(GameState.BLOCK_DECLARED);
    }

    public void declareChallenge(Game game, String challengerId, String challengedId, String challengeReason) {

        Player challenger = game.getPlayer(challengerId);
        ActionRecord challengeRecord = new ActionRecord(
                challengerId,
                ActionType.CHALLENGE,
                challengedId,
                null,
                challengeReason);

        setCurrentChallenger(challengerId);
        updateChallengeAnalytics(game, challenger, challengedId);
        logNewChallenge(game, challengeRecord);
        game.setState(GameState.CHALLENGE_DECLARED);
    }

    public void resolveChallenge(Game game, ActionRecord challengedRecord){
        Player challenger = game.getPlayer(challengerId);
        Player challenged = game.getPlayer(challengedRecord.getTargetId());


        //Challenger is winner
        if(challengedRecord.getActionIsBluff()){
            game.logGameMemory(challenger.getId() + " wins challenge");
            updateSuccessfulChallengeAnalytics(game, challenger, challenged, challengedRecord);
            setChallengeOutcome(challenger, challenged);

            game.getDeckService().removeCard(game, challenged);

            if(challengeOnBlock()){
                game.setState(GameState.APPLY_ACTION);
            } else{
                game.setState(GameState.APPLY_CHALLENGE);
            }



        } //Challenged is winner
        else{
            game.logGameMemory(challenger.getId() + " looses challenge");
            updateUnsuccessfulChallengeAnalytics(game, challenger, challenged, challengedRecord);
            setChallengeOutcome(challenged, challenger);

            game.getDeckService().removeCard(game, challenger);
            game.getDeckService().switchCard(game, challenged, RoleUtil.getCard(challengedRecord.getAction()));

            if(challengeOnBlock()){
                game.setState(GameState.APPLY_BLOCK);
            } else{
                game.setState(GameState.APPLY_ACTION);
            }
        }

    }

    public void updateSuccessfulChallengeAnalytics(Game game, Player challenger, Player challenged,
                                                   ActionRecord challengeRecord){
        challenged.incrementBluffsFailed();
        challenger.incrementChallengesWon();
        game.getGameAnalyticsService().logInteraction(new InteractionRecord(
                challenger.getId(),
                challenged.getId(),
                ActionType.CHALLENGE,
                true));


        if(challengeOnBlock()){
            challenged.incrementBlocksFailed();
        }
        //If challenge is targeted (includes block) create new failed interaction
        if(challengeRecord.getTargetId() != null) {
            game.getGameAnalyticsService().logInteraction(new InteractionRecord(
                    challengeRecord.getPlayerId(),
                    challengeRecord.getTargetId(),
                    challengeRecord.getAction(), false));
        }
    }

    public void updateUnsuccessfulChallengeAnalytics(Game game, Player challenger, Player challenged,
                                                   ActionRecord challengeRecord){
        /*
        Put this somewhere
         if (isBlockChallenge) {
            if(!game.getPlayer(game.getActingPlayerId()).hasCard(roleService.getCard(game.getDeclaredAction()))){
                game.getPlayer(game.getActingPlayerId()).incrementBluffsFailed();
            }
            if(game.getTargetId() != null) {
                game.getGameAnalyticsService().logInteraction(new InteractionRecord(game.getActingPlayerId(),
                        game.getTargetId(), game.getDeclaredAction(), false));

            }
         */
        challenger.incrementChallengesLost();
        game.getGameAnalyticsService().logInteraction(new InteractionRecord(
                challenger.getId(),
                challenged.getId(),
                ActionType.CHALLENGE,
                false));
    }



    public void updateChallengeAnalytics(Game game, Player challenger, String challengedId) {
        game.incrementTotalChallenges();
        challenger.incrementChallengesIssued();
        challenger.setLastChallengedProvider(challengedId);

    }

    public void updateBlockAnalytics(Game game, Player blocker, ActionRecord blockRecord) {
        blocker.incrementBlocksIssued();

        if(blockRecord.getActionIsBluff()) {
            blocker.incrementBluffsAttempted();
        }
        game.incrementTotalBlocks();

    }

    public void logNewBlock(Game game, ActionRecord blockRecord) {
        game.logGameMemory(blockRecord.getPlayerId() + " declares " + blockRecord.getAction() + " on " + blockRecord.getTargetId());
        game.logAction(blockRecord);
    }

    public void logNewChallenge(Game game, ActionRecord challengeRecord) {
        game.logAction(challengeRecord);
        game.logGameMemory(challengerId + " declares CHALLENGE on " + challengeRecord.getTargetId());
    }

    public void setCurrentBlock(ActionRecord blockRecord) {
        this.blockRecord = blockRecord;
    }

    public void setCurrentChallenger(String challengerId) {
        this.challengerId = challengerId;

    }

    public void setChallengeOutcome(Player winner, Player loser){
        this.challengeWinner = winner;
        this.challengeLoser = loser;
    }

    public Player getChallengeWinner() {
        return challengeWinner;
    }

    public Player getChallengeLoser() {
        return challengeLoser;
    }

    public void clearChallengeService(){
        this.challengerId = null;
        this.blockRecord = null;
    }

    public ActionRecord getBlockRecord() {
        return this.blockRecord;
    }

    public boolean challengeOnBlock(){
        return getBlockRecord() != null;
    }






}
