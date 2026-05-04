package com.example.coup_bench.model;

import com.example.coup_bench.RoleUtil;
import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {
    private static List<ActionType> bluffableActions;


    public static Boolean isPlayerBluffing(Player player, ActionType action) {
        if(!RoleUtil.isBluffableAction(action)) return null;
        for(CardType c : player.getCards()){
           if(RoleUtil.getCard(action).equals(c)){
               return false;
           }
        }
        return true;
    }
    public static boolean isPlayerAlive(Player player){
        return !player.getCards().isEmpty();

    }
}
