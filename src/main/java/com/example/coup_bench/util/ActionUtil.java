package com.example.coup_bench.util;

import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;

public class ActionUtil {
    public static boolean actionIsValid(Game game, String playerId, ActionType action,  String targetId) {

        Player player = game.getPlayer(playerId);

        if (player.getCoins() >= 10 && action != ActionType.COUP)
            return false;

        switch (action) {
            case ASSASSINATE -> {
                if (player.getCoins() < 3)
                    return false;
                if (targetId == null)
                    return false;
                if (targetId.equals(player.getId()))
                    return false;
                if (!game.getPlayer(targetId).isAlive())
                    return false;
            }
            case COUP -> {
                if (player.getCoins() < 7)
                    return false;
                if (targetId == null)
                    return false;
                if (targetId.equals(player.getId()))
                    return false;
                if (!game.getPlayer(targetId).isAlive())
                    return false;
            }
            case STEAL -> {
                if (targetId == null)
                    return false;
                if (targetId.equals(player.getId()))
                    return false;
                if (!game.getPlayer(targetId).isAlive())
                    return false;
                if (game.getPlayer(targetId).getCoins() == 0)
                    return false;
            }
            case TAX, FOREIGN_AID, INCOME, EXCHANGE -> {
                if (targetId != null)
                    return false;
            }
        }
        return true;
    }
    public static String getInvalidReason(Game game, Player player, ActionType action, String targetId) {


        if (player.getCoins() >= 10 && action != ActionType.COUP)
            return ("Must choose COUP if 10 coins or more");

        switch (action) {
            case ASSASSINATE -> {
                if (player.getCoins() < 3)
                    return ("Not enough coins to ASSASSINATE");
                if (targetId == null)
                    return ("ASSASSINATE requires a target");
                if (targetId.equals(player.getId()))
                    return ("Cannot ASSASSINATE yourself");
                if (!game.getPlayer(targetId).isAlive())
                    return ("ASSASSINATE requires an alive target");
            }
            case COUP -> {
                if (player.getCoins() < 7)
                    return "Not enough coins to COUP";
                if (targetId == null)
                    return "COUP requires a target";
                if (targetId.equals(player.getId()))
                    return "Cannot COUP yourself";
                if (!game.getPlayer(targetId).isAlive())
                    return "COUP requires an alive target";
            }
            case STEAL -> {
                if (targetId == null)
                    return "STEAL requires a target";
                if (targetId.equals(player.getId()))
                    return "Cannot STEAL from yourself";
                if (!game.getPlayer(targetId).isAlive())
                    return "STEAL requires an alive target";
                if (game.getPlayer(targetId).getCoins() == 0)
                    return "STEAL requires a target with more than 0 coins";
            }
            case TAX, FOREIGN_AID, INCOME, EXCHANGE -> {
                if (targetId != null)
                    return action + " must not have a target";
            }
        }
        return null;
    }
}
