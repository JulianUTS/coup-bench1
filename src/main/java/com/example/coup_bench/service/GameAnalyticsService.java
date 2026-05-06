package com.example.coup_bench.service;

import com.example.coup_bench.model.*;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.repoModels.InteractionRecord;
import com.example.coup_bench.model.repoModels.TurnSnapshot;
import com.example.coup_bench.util.ActionUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class GameAnalyticsService {
    public void logDeclaredAction(Game game, Player player, ActionRecord actionRecord){
        PlayerStats playerStats = player.getPlayerStats();
        GameStats gameStats = game.getGameStats();
        //Update bluff values
        if (actionRecord.getActionIsBluff()) {
            playerStats.incrementBluffsAttempted();
            gameStats.logBluff(actionRecord);
        }

        //Update action values
        switch(actionRecord.getAction()) {
            case STEAL -> playerStats.incrementStealAttempts();
            case ASSASSINATE -> playerStats.incrementAssassinationAttempts();
            case EXCHANGE -> playerStats.incrementExchangeAttempts();
            case FOREIGN_AID -> playerStats.incrementForeignAidAttempts();
            case TAX -> playerStats.incrementTaxAttempts();
        }

    }
    public void logSuccessfulAction(Game game, Player player, ActionRecord actionRecord) {
        PlayerStats playerStats = player.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        if (actionRecord.getActionIsBluff()) {
            playerStats.incrementBluffsSuccessful();
        }

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


    }
    public void logUnsuccessfulAction(GameStats gameStats, PlayerStats playerStats){

    }
    public void logDeclaredBlock(Game game, Player blocker, ActionRecord blockRecord){
        PlayerStats blockerStats = blocker.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        blockerStats.incrementBlocksIssued();

        if(blockRecord.getActionIsBluff()) {
            blockerStats.incrementBluffsAttempted();
        }

        gameStats.incrementTotalBlocks();

    }
    public void logSuccessfulBlock(GameStats gameStats, PlayerStats playerStats){

    }
    public void logUnsuccessfulBlock(GameStats gameStats, PlayerStats playerStats){

    }
    public void logDeclaredChallenge(Game game, Player challenger, String challengedId){
        PlayerStats challengerStats = challenger.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        gameStats.incrementTotalChallenges();
        challengerStats.incrementChallengesIssued();
        challengerStats.addChallengeTarget(challengedId);


    }
    public void logSuccessfulChallenge(Game game, Player challenger, Player challenged,
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
    public void logUnsuccessfulChallenge(Game game, Player challenger, Player challenged){
        PlayerStats challengerStats = challenger.getPlayerStats();
        GameStats gameStats = game.getGameStats();

        challengerStats.incrementChallengesLost();
        gameStats.logInteraction(new InteractionRecord(
                challenger.getId(),
                challenged.getId(),
                ActionType.CHALLENGE,
                false));
    }




}
