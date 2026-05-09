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
import com.example.coup_bench.util.StatsUtil;
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
    private final AiReactionService AiReaction;

    public ChallengeService(AiReactionService aiReactionService){
        this.AiReaction = aiReactionService;
    }

    public void declareBlock(Game game, AiReaction block) {

        Player blocker = game.getPlayer(block.id);
        ActionRecord blockRecord = new ActionRecord(
                block.id,
                block.action,
                block.targetId,
                PlayerUtil.isPlayerBluffing(game.getPlayer(blocker.getId()), block.action),
                block.reason);

        StatsUtil.logDeclaredBlock(game, blocker, blockRecord);
        logNewBlock(game, blockRecord);
        setCurrentBlock(blockRecord);
        game.setState(GameState.BLOCK_DECLARED);
    }

    public void declareChallenge(Game game, AiReaction challenge) {

        Player challenger = game.getPlayer(challenge.id);
        ActionRecord challengeRecord = new ActionRecord(
                challenge.id,
                ActionType.CHALLENGE,
                challenge.targetId,
                null,
                challenge.reason);

        setCurrentChallenger(challengerId);
        StatsUtil.logDeclaredChallenge(game, challenger, challenge.id);
        logNewChallenge(game, challengeRecord);
        game.setState(GameState.CHALLENGE_DECLARED);
    }

    public void resolveChallenge(Game game, DeckService deckService, ActionRecord challengedRecord){
        Player challenger = game.getPlayer(challengerId);
        Player challenged = game.getPlayer(challengedRecord.getTargetId());


        //Challenger is winner
        if(challengedRecord.getActionIsBluff()){
            game.logGameMemory(challenger.getId() + " wins challenge");
            StatsUtil.logSuccessfulChallenge(game, challenger, challenged, challengedRecord, challengeOnBlock());
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
            StatsUtil.logUnsuccessfulChallenge(game, challenger, challenged);
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

    public void resolveBlock(Game game, ActionRecord actionRecord) {
        Predicate<Player> filter = p -> p.isAlive() &&
                !p.getId().equals(actionRecord.getPlayerId()) &&
                !p.getId().equals(blockRecord.getPlayerId());
        loopChallengers(game, blockRecord, challengeScenario, filter);
    }

    public void applyS_1(Game game, ActionRecord actionRecord) {
        Predicate<Player> filter = p -> p.isAlive() && !p.getId().equals(actionRecord.getPlayerId());
        loopChallengers(game, actionRecord, Scenario.S1, filter);
        if(noChallenge()){
            game.setState(GameState.APPLY_ACTION);
        }
    }
    public void applyS_2_1(Game game, ActionRecord actionRecord) {
        Predicate<Player> filter = p -> p.isAlive() && !p.getId().equals(actionRecord.getPlayerId());
        loopChallengers(game, actionRecord, Scenario.S2_1, filter);
        if(!noBlock()){
            setChallengeScenario(Scenario.S2_2);
        }
    }

    public void applyS_3_1(Game game, ActionRecord actionRecord) {
        Predicate<Player> filter  = p -> p.isAlive() &&
                !p.getId().equals(actionRecord.getPlayerId()) &&
                !p.getId().equals(actionRecord.getTargetId());
        loopChallengers(game, actionRecord, Scenario.S3_1, filter);
        if(noChallenge()){
            setChallengeScenario(Scenario.S3_2);
        }

    }
    public void applyS_3_2(Game game, ActionRecord actionRecord) {
        Player blocker = game.getPlayer(actionRecord.getTargetId());
        AiReaction block = askChallenger(game, actionRecord, blocker, Scenario.S3_2);
        if (block.action != ActionType.DO_NOTHING){
            declareBlock(game, block);
            setChallengeScenario(Scenario.S3_3);

        } else{
            game.logAction(new ActionRecord(blocker.getId(), ActionType.DO_NOTHING, null, null, block.reason));
        }
    }

    public void applyS_4_1(Game game, ActionRecord actionRecord) {
        Predicate<Player> filter  = p -> p.isAlive() &&
                !p.getId().equals(actionRecord.getPlayerId());
        loopChallengers(game, actionRecord, Scenario.S4_1, filter);
        if(noChallenge()){
            setChallengeScenario(Scenario.S4_2);
        }
    }

    public void applyS_4_2(Game game, ActionRecord actionRecord) {
        Player blocker = game.getPlayer(actionRecord.getTargetId());
        AiReaction block = askChallenger(game, actionRecord, blocker, Scenario.S4_2);
        if (block.action != ActionType.DO_NOTHING){
            declareBlock(game, block);
            setChallengeScenario(Scenario.S4_3);

        } else{
            game.logAction(new ActionRecord(blocker.getId(), ActionType.DO_NOTHING, null, null, block.reason));
        }
    }


    private void loopChallengers(Game game, ActionRecord challengedRecord, Scenario scenario, Predicate<Player> filter){
        String challengedPlayerId = challengedRecord.getPlayerId();

        int startIndex = game.getPlayerIndex(challengedRecord.getPlayerId());
        int playerCount = game.getPlayers().size();

        for (int i = shiftIndex; i < playerCount; i++) {
            incrementShiftIndex();

            Player challenger = game.getPlayers().get((startIndex + i) % playerCount);
            if (!filter.test(challenger)) continue;

            if (challenger.isHuman()) {
                game.setState(GameState.WAITING_FOR_HUMAN_ACTION);
                return;
            }

            AiReaction challenge = askChallenger(game, challengedRecord, challenger, scenario);

            switch (challenge.action) {
                case CHALLENGE:
                    challenge.id = challenger.getId();
                    challenge.targetId = challengedPlayerId;
                    declareChallenge(game, challenge);
                    return;
                case BLOCK_USING_DUKE:
                    challenge.id = challenger.getId();
                    challenge.targetId = challengedPlayerId;
                    declareBlock(game, challenge);
                    return;
                default:
                    game.logAction(new ActionRecord(challenger.getId(), ActionType.DO_NOTHING, null, null, challenge.reason));
            }
        }
        resetShiftIndex();
        if(challengeOnBlock()){
            game.setState(GameState.APPLY_BLOCK);
        } else {
            game.setState(GameState.APPLY_ACTION);
        }

    }
    private AiReaction askChallenger(Game game, ActionRecord actionRecord, Player player, Scenario scenario) {

        AiReaction reaction = AiReaction.getReaction(game, actionRecord, this, player, scenario);

        if (reaction.action == ActionType.DO_NOTHING) {
            game.logAction(new ActionRecord(player.getId(), ActionType.DO_NOTHING, null, null, reaction.reason));
        }
        return reaction;
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

    public boolean noChallenge(){
        return challengerId == null;
    }

    public boolean noBlock(){
        return blockRecord == null;
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
        this.challengeScenario = null;
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
    private void incrementShiftIndex(){
        this.shiftIndex++;
    }
    private void resetShiftIndex(){
        this.shiftIndex =0;
    }






}
