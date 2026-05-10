package com.example.coup_bench.model;

import com.example.coup_bench.model.Enums.CardType;

public class HumanChooseCardRequest {

    private CardType card;


    public CardType getCard() {
        return card;
    }

    public void setCard(CardType card) {
        this.card = card;
    }
}

