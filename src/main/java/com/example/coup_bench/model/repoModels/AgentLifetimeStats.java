package com.example.coup_bench.model.repoModels;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "agent_stats")
public class AgentLifetimeStats {

    @Id
    private String provider;

    private int totalGames;
    private int wins;
    private int losses;

    private Map<String, PersonalityStats> personalities = new HashMap<>();

    public AgentLifetimeStats() {}

    public AgentLifetimeStats(String provider) {
        this.provider = provider;
    }

    // -------- Getters & Setters --------

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public Map<String, PersonalityStats> getPersonalities() {
        return personalities;
    }

    public void setPersonalities(Map<String, PersonalityStats> personalities) {
        this.personalities = personalities;
    }
}

