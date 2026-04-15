package com.example.coup_bench.old;

import com.example.coup_bench.model.GameState;
import com.example.coup_bench.model.Player;

import java.util.List;

public record CoupTestSnapshot(
        GameState state,
        List<Player> players,
        String currentTurnPlayerId,
        String lastAction
) {}

