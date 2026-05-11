package com.example.coup_bench.util;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.repoModels.InteractionRecord;
import com.example.coup_bench.model.repoModels.TurnSnapshot;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class StatsUtil {
    public static void logDeclaredAction(Game game, Player player, ActionRecord actionRecord){
        PlayerStats playerStats = player.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        if(ActionUtil.isTargetedAction(actionRecord.getAction())){
            playerStats.addActionTarget(actionRecord.getTargetId());
        }

        //Update action values
        switch(actionRecord.getAction()) {
            case STEAL -> playerStats.incrementStealAttempts();
            case ASSASSINATE -> playerStats.incrementAssassinationAttempts();
            case EXCHANGE -> playerStats.incrementExchangeAttempts();
            case FOREIGN_AID -> playerStats.incrementForeignAidAttempts();
            case TAX -> playerStats.incrementTaxAttempts();
        }
        if(!RoleUtil.isBluffableAction(actionRecord.getAction())) return;
        if (actionRecord.getActionIsBluff()) {
            playerStats.incrementBluffsAttempted();
            gameStats.logBluff(actionRecord);
        }


    }
    public static void logSuccessfulAction(Game game, Player player, ActionRecord actionRecord) {
        PlayerStats playerStats = player.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        if (ActionUtil.isTargetedAction(actionRecord.getAction())) {
            gameStats.logInteraction(new InteractionRecord(
                    actionRecord.getPlayerId(),
                    actionRecord.getTargetId(),
                    actionRecord.getAction(), true));
        }

        switch (actionRecord.getAction()) {
            case INCOME -> playerStats.incrementIncomeCount();
            case COUP -> playerStats.incrementCoupsCount();
            case FOREIGN_AID -> playerStats.incrementForeignAidSuccessful();
            case TAX -> playerStats.incrementTaxSuccesses();
            case STEAL -> playerStats.incrementStealSuccesses();
            case ASSASSINATE -> playerStats.incrementAssassinationSuccesses();
            case EXCHANGE -> playerStats.incrementExchangeSuccessful();
        }
        if(!RoleUtil.isBluffableAction(actionRecord.getAction())) return;
        if (actionRecord.getActionIsBluff()) {
            playerStats.incrementBluffsSuccessful();
        }
    }
    public static void logFailedAction(Game game, ActionRecord actionRecord) {
        GameStats gameStats = game.getGameStats();
        gameStats.logInteraction(new InteractionRecord(
                actionRecord.getPlayerId(),
                actionRecord.getTargetId(),
                actionRecord.getAction(), false));
    }
    public static void logDeclaredBlock(Game game, Player blocker, ActionRecord blockRecord){
        PlayerStats blockerStats = blocker.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        blockerStats.incrementBlocksIssued();
        blockerStats.addBlockTarget(blockRecord.getTargetId());

        if(blockRecord.getActionIsBluff()) {
            blockerStats.incrementBluffsAttempted();
        }

        gameStats.incrementTotalBlocks();

    }
    public static void logSuccessfulBlock(Game game, Player blocker, Player blocked, ActionRecord blockAction ,ActionRecord blockedAction){
        PlayerStats blockerStats = blocker.getPlayerStats();
        GameStats gameStats = game.getGameStats();
        PlayerStats blockedStats = blocked.getPlayerStats();

        blockerStats.incrementBlocksSuccessful();
        blockedStats.incrementedBlocked();

        if(ActionUtil.isTargetedAction(blockedAction.getAction())) {
            gameStats.logInteraction(new InteractionRecord(
                    blockedAction.getPlayerId(),
                    blockedAction.getTargetId(),
                    blockedAction.getAction(), false));
        }

        if(blockAction.getActionIsBluff()) {
            blockerStats.incrementBluffsSuccessful();
        }
        if(!RoleUtil.isBluffableAction(blockedAction.getAction())) return;
        if(blockedAction.getActionIsBluff()){
            blockedStats.incrementBluffsFailed();
        }
    }

    public static void logDeclaredChallenge(Game game, Player challenger, String challengedId){
        PlayerStats challengerStats = challenger.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        gameStats.incrementTotalChallenges();
        challengerStats.incrementChallengesIssued();
        challengerStats.addChallengeTarget(challengedId);


    }
    public static void logSuccessfulChallenge(Game game, Player challenger, Player challenged,
                                       ActionRecord challengeRecord, boolean onBlock){
        PlayerStats challengerStats = challenger.getPlayerStats();
        PlayerStats challengedStats = challenged.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        challengedStats.incrementBluffsFailed();
        challengerStats.incrementChallengesWon();

        gameStats.logInteraction(new InteractionRecord(
                challenger.getId(),
                challenged.getId(),
                ActionType.CHALLENGE,
                true));


        if(onBlock){
            challengedStats.incrementBlocksFailed();
        }
        //If challenge is targeted (includes block) create new failed interaction
        if(ActionUtil.isTargetedAction(challengeRecord.getAction())) {
            gameStats.logInteraction(new InteractionRecord(
                    challengeRecord.getPlayerId(),
                    challengeRecord.getTargetId(),
                    challengeRecord.getAction(), false));
        }
    }
    public static void logUnsuccessfulChallenge(Game game, Player challenger, Player challenged){
        PlayerStats challengerStats = challenger.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        challengerStats.incrementChallengesLost();
        gameStats.logInteraction(new InteractionRecord(
                challenger.getId(),
                challenged.getId(),
                ActionType.CHALLENGE,
                false));
    }

    public static void logTurnSnapshot(Game game, ActionRecord actionRecord){
        GameStats gameStats = game.getGameStats();
        TurnSnapshot snap = new TurnSnapshot(
                game.getTurn(),
                game.getPlayers().stream()
                        .collect(Collectors.toMap(Player::getId, Player::getCoins)),
                game.getPlayers().stream()
                        .collect(Collectors.toMap(Player::getId, p -> p.getCards().size())),
                actionRecord.getAction(),
                actionRecord.getPlayerId(),
                actionRecord.getTargetId()
        );

        gameStats.logTurnSnapshot(snap);
    }

    public static void incrementTurnsSurvived(List<Player> players){
        for (Player player:  players){
            if(player.isAlive()){
                PlayerStats playerStats = player.getPlayerStats();
                playerStats.incrementTurnsSurvived();
            }
        }
    }

    public static void logPlayerKilled(Player killer, Player killed){
        PlayerStats killerStats = killer.getPlayerStats();
        PlayerStats killedStats = killed.getPlayerStats();

        killerStats.addPlayersKilled(killed.getId());
        killedStats.setKilledBy(killer.getId());
    }


    /*
                    Game scenarios:
                    1- Tax/Exchange
                    Action can be challenged by anyone, no block

                    2- Foreign aid
                    Action can be blocked by anyone, blocked can be challenged by anyone

                    3- Steal
                    Action can be challenged by anyone except for targeted player, action can be blocked by targeted player,
                    counter can be challenged by anyone except for target and original player

                    4- Assassination
                    Action can be challenged by anyone, action can be blocked by targeted player,
                    counter can be challenged by anyone except for target and original player
                     */
    public enum ChallengeScenario {
        S1,
        S2_1,
        S2_2,
        S3_1,
        S3_2,
        S3_3,
        S4_1,
        S4_2,
        S4_3
    }
}
