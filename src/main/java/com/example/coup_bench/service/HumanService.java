package com.example.coup_bench.service;

import com.example.coup_bench.model.Enums.GameState;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.PromptUtil;
import org.springframework.stereotype.Service;


@Service
public class HumanService {
   private GameState previous;
    private String currentPrompt;

    public GameState getPrevious() {
        return previous;
    }

    public void setPrevious(GameState previous) {
        this.previous = previous;
    }
    public String getCurrentPrompt() {
        return currentPrompt;
    }

    public void setCurrentPrompt(String currentPrompt) {
        this.currentPrompt = currentPrompt;
    }

}
