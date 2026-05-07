package com.example.coup_bench.service;

import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.Enums.Scenario;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.PlayerUtil;
import com.example.coup_bench.util.RoleUtil;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;


@Service
public class ChallengeService {
    private ActionRecord blockRecord;
    private String challengerId;
    private Player challengeWinner;
    private Player challengeLoser;
    private int shiftIndex = 0;
    private Scenario challengeScenario;

    public void declareBlock(Game game, GameAnalyticsService stats, String blockerId,
                             ActionType blockAction, String blockedId, String blockReason) {

        Player blocker = game.getPlayer(blockerId);
        ActionRecord blockRecord = new ActionRecord(
                blockerId,
                blockAction,
                blockedId,
                PlayerUtil.isPlayerBluffing(game.getPlayer(blockerId), blockAction),
                blockReason);

        stats.logDeclaredBlock(game, blocker, blockRecord);
        logNewBlock(game, blockRecord);
        setCurrentBlock(blockRecord);
        game.setState(GameState.BLOCK_DECLARED);
    }

    public void declareChallenge(Game game, GameAnalyticsService stats, String challengerId, String challengedId, String challengeReason) {

        Player challenger = game.getPlayer(challengerId);
        ActionRecord challengeRecord = new ActionRecord(
                challengerId,
                ActionType.CHALLENGE,
                challengedId,
                null,
                challengeReason);

        setCurrentChallenger(challengerId);
        stats.logDeclaredChallenge(game, challenger, challengedId);
        logNewChallenge(game, challengeRecord);
        game.setState(GameState.CHALLENGE_DECLARED);
    }

    public void resolveChallenge(Game game, GameAnalyticsService stats, DeckService deckService, ActionRecord challengedRecord){
        Player challenger = game.getPlayer(challengerId);
        Player challenged = game.getPlayer(challengedRecord.getTargetId());


        //Challenger is winner
        if(challengedRecord.getActionIsBluff()){
            game.logGameMemory(challenger.getId() + " wins challenge");
            stats.logSuccessfulChallenge(game, challenger, challenged, challengedRecord, challengeOnBlock());
            setChallengeOutcome(challenger, challenged);
            deckService.removePlayerCard(game, challenged);

            if(challengeOnBlock()){
                game.setState(GameState.APPLY_ACTION);
            } else{
                game.setState(GameState.APPLY_CHALLENGE);
            }



        } //Challenged is winner
        else{
            game.logGameMemory(challenger.getId() + " looses challenge");
            stats.logUnsuccessfulChallenge(game, challenger, challenged);
            setChallengeOutcome(challenged, challenger);

            deckService.removePlayerCard(game, challenger);
            deckService.switchPlayerCard(game, challenged, RoleUtil.getCard(challengedRecord.getAction()));

            if(challengeOnBlock()){
                game.setState(GameState.APPLY_BLOCK);
            } else{
                game.setState(GameState.APPLY_ACTION);
            }
        }

    }

    public void applyS_1(Game game, ChallengeService challengeService, AiDecisionService ai) {
        int playerIndex = game.getPlayerIndex()
        game = findChallenger(
                game, playerIndex,
                p -> p.isAlive() && !p.getId().equals(playerId),
                1
        );

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

    public String getBlockerId() {
        return blockRecord.getPlayerId();
    }
    public Player getBlocker(Game game) {
        return game.getPlayer(blockRecord.getPlayerId());
    }

    public ActionType getBlockAction(){
        return getBlockRecord().getAction();
    }
    public void setChallengeScenario(Scenario challengeScenario){
        this.challengeScenario = challengeScenario;
    }
    public Scenario getChallengeScenario(){
        return challengeScenario;
    }

    public Boolean blockIsBluff(){
        return getBlockRecord().getActionIsBluff();
    }
    private void incrementShiftIndex(){
        this.shiftIndex++;
    }

    private Game findChallenger(Game game, ActionRecord action, Predicate<Player> filter, int reactionCode) {
        int startIndex =
        int playerCount = game.getPlayers().size();

        for (int i = shiftIndex; i < playerCount; i++) {
            Player p = game.getPlayers().get((startIndex + i) % playerCount);
            if (!filter.test(p)) continue;

            AiReaction reaction = ai.getReaction(game, p, reactionCode);

            if (reaction.action == ActionType.CHALLENGE) {
                declareChallenge(game, p.getId(), reaction);
                game = coup.resolveChallenge(game, ai);
                return game;
            } else if(reaction.action == ActionType.BLOCK_USING_DUKE){
                game = coup.declareBlock(game, p.getId(), reaction);
                return game;
            } else{
                game = coup.logAction(game, new ActionRecord(p.getId(), reaction.action, null, reaction.reason));
            }
        }
        return game;
    }




}
