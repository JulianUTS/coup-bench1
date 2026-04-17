package com.example.coup_bench.model;

public class AiStats {
    public int wins;
    public int losses;
    public int totalGames;
    public int totalActions;
    public int totalBluffs;

    public void recordWin() { wins++; totalGames++; }
    public void recordLoss() { losses++; totalGames++; }
    public void recordAction() { totalActions++; }
    public void recordBluff() { totalBluffs++; }
}

