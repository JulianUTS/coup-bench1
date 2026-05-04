package com.example.coup_bench;

import com.example.coup_bench.model.Enums.ActionType;
import com.example.coup_bench.model.Enums.CardType;

public class RoleUtil {
    public static CardType getCard(ActionType action) {
        return switch (action) {
            case TAX -> CardType.DUKE;
            case STEAL -> CardType.CAPTAIN;
            case ASSASSINATE -> CardType.ASSASSIN;
            case EXCHANGE -> CardType.AMBASSADOR;
            case BLOCK_USING_AMBASSADOR -> CardType.AMBASSADOR;
            case BLOCK_USING_CAPTAIN -> CardType.CAPTAIN;
            case BLOCK_USING_CONTESSA -> CardType.CONTESSA;
            case BLOCK_USING_DUKE -> CardType.DUKE;

            default -> null;
        };
    }

    public static ActionType getAction(CardType card) {
        return switch (card) {
            case AMBASSADOR -> ActionType.BLOCK_USING_AMBASSADOR;
            case CAPTAIN -> ActionType.BLOCK_USING_CAPTAIN;
            case CONTESSA -> ActionType.BLOCK_USING_CONTESSA;
            case DUKE -> ActionType.BLOCK_USING_DUKE;

            default -> null;
        };
    }

    public static Boolean isBluffableAction(ActionType action) {
        return switch (action) {
            case BLOCK_USING_CAPTAIN, ASSASSINATE, EXCHANGE, STEAL, TAX, BLOCK_USING_DUKE, BLOCK_USING_CONTESSA,
                 BLOCK_USING_AMBASSADOR -> true;

            default -> false;
        };

    }
}


