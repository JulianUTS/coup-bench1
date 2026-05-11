package com.example.coup_bench.model.humanResponses;

import com.example.coup_bench.model.Enums.CardType;

import java.util.List;

public class HumanExchangeCardRequest {
    private List<CardType> cardsToKeep;


    public List<CardType> getCardsToKeep() {
        return cardsToKeep;
    }
}
