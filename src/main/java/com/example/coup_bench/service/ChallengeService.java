package com.example.coup_bench.service;

import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.AiResponses.AiReaction;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.Enums.Scenario;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.humanResponses.HumanReactionRequest;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.HumanUtil;
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

    public void getHumanReaction(Game game, HumanReactionRequest humanReaction, String targetId){
        AiReaction reaction = new AiReaction();
        reaction.action = humanReaction.getReaction();
        reaction.targetId = targetId;
        reaction.id = "human";

        switch (humanReaction.getReaction()) {
            case CHALLENGE:
                declareChallenge(game, reaction);
                return;
            case DO_NOTHING:
                game.logAction(new ActionRecord(reaction.id, ActionType.DO_NOTHING, null, null, null));
                game.setState(GameState.WAITING_FOR_CHALLENGE);
                return;
            default:
                declareBlock(game, reaction);
                switch (challengeScenario){
                    case S2_1 -> setChallengeScenario(Scenario.S2_2);
                    case S3_2 -> setChallengeScenario(Scenario.S3_3);
                    case S4_2 -> setChallengeScenario(Scenario.S4_3);
                }
        }
    }

    public void declareBlock(Game game, AiReaction block) {

        Player blocker = game.getPlayer(block.id);
        ActionRecord blockRecord = new ActionRecord(
                block.id,
                block.action,
                block.targetId,
                PlayerUtil.isPlayerBluffing(game.getPlayer(blocker.getId()), block.action),
                block.reason);
        if(blockRecord.getTargetId() == null){
            System.out.print(blockRecord);}

        logNewBlock(game, blockRecord);
        setCurrentBlock(blockRecord);
        StatsUtil.logDeclaredBlock(game, blocker, blockRecord);
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
        if(challengeRecord.getTargetId() == null){
            System.out.print(challengeRecord);}

        setCurrentChallenger(challenge.id);
        logNewChallenge(game, challengeRecord);
        StatsUtil.logDeclaredChallenge(game, challenger, challenge.targetId);
        game.setState(GameState.CHALLENGE_DECLARED);
    }

    public void resolveChallenge(Game game, DeckService deckService, ActionRecord challengedRecord,HumanService human){
        Player challenger = game.getPlayer(challengerId);
        Player challenged = game.getPlayer(challengedRecord.getPlayerId());

        //Challenger is winner
        if(challengedRecord.getActionIsBluff()){
            challengerWins(game, deckService, challengedRecord, challenger, challenged, human);
        } //Challenged is winner
        else{
            challengerLost(game, deckService, challengedRecord, challenger, challenged, human);
        }
        if(PlayerUtil.onlyOneLeft(game.getPlayers())){
            game.setState(GameState.ENDGAME);
        }
    }

    public void challengerWins(Game game, DeckService deckService, ActionRecord challengedRecord,
                               Player challenger, Player challenged, HumanService human){
        game.logGameMemory(challenger.getId() + " wins challenge");
        setChallengeOutcome(challenger, challenged);
        StatsUtil.logSuccessfulChallenge(game, challenger, challenged, challengedRecord, challengeOnBlock());

        if(challengeOnBlock()){
            game.setState(GameState.APPLY_ACTION);
        } else{
            game.setState(GameState.NEXT_TURN);
        }

        if(challenged.isHuman() && challenged.getCards().size() > 1){
            human.setCurrentPrompt(HumanUtil.printGetCardPrompt(game,challenged));
            human.setPrevious(game.getState());
            game.setState(GameState.WAITING_FOR_HUMAN_ACTION);
            return;
        }

        if(!deckService.removePlayerCard(game, challenged)){
            StatsUtil.logPlayerKilled(challenger, challenged, ActionType.CHALLENGE);
            game.logGameMemory(challenged.getId() + " has lost all their cards");
        }

    }
    public void challengerLost(Game game, DeckService deckService, ActionRecord challengedRecord,
                               Player challenger, Player challenged, HumanService human){
        game.logGameMemory(challenger.getId() + " looses challenge");
        setChallengeOutcome(challenged, challenger);
        StatsUtil.logUnsuccessfulChallenge(game, challenger, challenged);

        if(challengeOnBlock()){
            StatsUtil.logSuccessfulBlock(game,
                    getBlocker(game),
                    game.getPlayer(challengedRecord.getPlayerId()),
                    getBlockRecord(),
                    challengedRecord);
            game.setState(GameState.NEXT_TURN);
        } else{
            game.setState(GameState.APPLY_ACTION);
        }

        deckService.switchPlayerCard(game, challenged, RoleUtil.getCard(challengedRecord.getAction()));

        if(challenger.isHuman() && challenger.getCards().size() > 1){
            human.setCurrentPrompt(HumanUtil.printGetCardPrompt(game,challenger));
            human.setPrevious(game.getState());
            game.setState(GameState.WAITING_FOR_HUMAN_ACTION);
            return;
        }

        if(!deckService.removePlayerCard(game, challenger)){
            StatsUtil.logPlayerKilled(challenged, challenger, ActionType.CHALLENGE);
            game.logGameMemory(challenger.getId() + " has lost all their cards");
            if(challengedRecord.getAction() == ActionType.ASSASSINATE && challenger.getId().equals(challengedRecord.getTargetId())){
                StatsUtil.logFailedAction(game, challengedRecord);
                game.setState(GameState.NEXT_TURN);
            }
        }






    }

    public void resolveBlock(Game game, ActionRecord actionRecord, HumanService human) {
        Predicate<Player> filter = p -> p.isAlive() &&
                !p.getId().equals(actionRecord.getPlayerId()) &&
                !p.getId().equals(blockRecord.getPlayerId());
        loopChallengers(game, blockRecord, challengeScenario, filter, human);
        if(noChallenge()){
            StatsUtil.logSuccessfulBlock(game,
                    getBlocker(game),
                    game.getPlayer(actionRecord.getPlayerId()),
                    getBlockRecord(),
                    actionRecord);
            game.setState(GameState.NEXT_TURN);
        }
    }

    public void applyS_1(Game game, ActionRecord actionRecord, HumanService human) {
        Predicate<Player> filter = p -> p.isAlive() && !p.getId().equals(actionRecord.getPlayerId());
        loopChallengers(game, actionRecord, Scenario.S1, filter, human);
        if(game.getState() == GameState.WAITING_FOR_HUMAN_ACTION){
            return;
        }
        if(noChallenge()){
            game.setState(GameState.APPLY_ACTION);
        }
    }
    public void applyS_2_1(Game game, ActionRecord actionRecord, HumanService human) {
        Predicate<Player> filter = p -> p.isAlive() && !p.getId().equals(actionRecord.getPlayerId());
        loopChallengers(game, actionRecord, Scenario.S2_1, filter, human);
        if(game.getState() == GameState.WAITING_FOR_HUMAN_ACTION){
            return;
        }
        if(!blockDetected()){
            game.setState(GameState.APPLY_ACTION);
        } else{
            setChallengeScenario(Scenario.S2_2);
        }
    }

    public void applyS_3_1(Game game, ActionRecord actionRecord, HumanService human) {
        Predicate<Player> filter  = p -> p.isAlive() &&
                !p.getId().equals(actionRecord.getPlayerId()) &&
                !p.getId().equals(actionRecord.getTargetId());
        loopChallengers(game, actionRecord, Scenario.S3_1, filter, human);
        if(game.getState() == GameState.WAITING_FOR_HUMAN_ACTION){
            return;
        }
        if(noChallenge()){
            setChallengeScenario(Scenario.S3_2);
        }

    }
    public void applyS_3_2(Game game, ActionRecord actionRecord, HumanService human) {
        Player blocker = game.getPlayer(actionRecord.getTargetId());
        if (blocker.isHuman()) {
            human.setCurrentPrompt(HumanUtil.printGetReactionPrompt(game, actionRecord, this, blocker, Scenario.S3_2));
            game.setState(GameState.WAITING_FOR_HUMAN_ACTION);
            return;
        }
        AiReaction block = askChallenger(game, actionRecord, blocker, Scenario.S3_2);
        if (block.action != ActionType.DO_NOTHING){
            setChallengeScenario(Scenario.S3_3);
            block.id = blocker.getId();
            block.targetId = actionRecord.getPlayerId();
            declareBlock(game, block);
        } else{
            game.logAction(new ActionRecord(blocker.getId(), ActionType.DO_NOTHING, null, null, block.reason));
            game.setState(GameState.APPLY_ACTION);
        }
    }

    public void applyS_4_1(Game game, ActionRecord actionRecord, HumanService human) {
        Predicate<Player> filter  = p -> p.isAlive() &&
                !p.getId().equals(actionRecord.getPlayerId());
        loopChallengers(game, actionRecord, Scenario.S4_1, filter, human);
        if(game.getState() == GameState.WAITING_FOR_HUMAN_ACTION){
            return;
        }
        if(noChallenge()){
            setChallengeScenario(Scenario.S4_2);
        }
    }

    public void applyS_4_2(Game game, ActionRecord actionRecord, HumanService human) {
        Player blocker = game.getPlayer(actionRecord.getTargetId());
        if (blocker.isHuman()) {
            human.setCurrentPrompt(HumanUtil.printGetReactionPrompt(game, actionRecord, this, blocker, Scenario.S4_2));
            game.setState(GameState.WAITING_FOR_HUMAN_ACTION);
            return;
        }

        AiReaction block = askChallenger(game, actionRecord, blocker, Scenario.S4_2);
        if (block.action != ActionType.DO_NOTHING){
            block.id = blocker.getId();
            block.targetId = actionRecord.getPlayerId();
            setChallengeScenario(Scenario.S4_3);
            declareBlock(game, block);
        } else{
            game.logAction(new ActionRecord(blocker.getId(), ActionType.DO_NOTHING, null, null, block.reason));
            game.setState(GameState.APPLY_ACTION);
        }
    }

    private void loopChallengers(Game game, ActionRecord challengedRecord, Scenario scenario, Predicate<Player> filter,
                                 HumanService human){
        String challengedPlayerId = challengedRecord.getPlayerId();

        int startIndex = game.getPlayerIndex(challengedRecord.getPlayerId());
        int playerCount = game.getPlayers().size();

        for (int i = shiftIndex; i < playerCount; i++) {
            incrementShiftIndex();

            Player challenger = game.getPlayers().get((startIndex + i) % playerCount);
            if (!filter.test(challenger)) continue;

            if (challenger.isHuman()) {
                human.setCurrentPrompt(HumanUtil.printGetReactionPrompt(game, challengedRecord, this, challenger, scenario));
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
    }
    private AiReaction askChallenger(Game game, ActionRecord actionRecord, Player player, Scenario scenario) {
        return AiReaction.getReaction(game, actionRecord, this, player, scenario);
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

    public boolean blockDetected(){
        return this.blockRecord != null;
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
        this.challengeWinner = null;
        this.challengeLoser  = null;
        this.shiftIndex =0;
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
