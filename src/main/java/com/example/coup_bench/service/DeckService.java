package com.example.coup_bench.service;

import com.example.coup_bench.model.Enums.CardType;
import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.Player;
import com.example.coup_bench.util.PlayerUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeckService {
    private final Deque<CardType> deck = new ArrayDeque<>();
    private final AiChooseCardService aiChooseCardService;

    public DeckService(AiChooseCardService aiChooseCardService) {
        this.aiChooseCardService = aiChooseCardService;
    }

    public void initializeDeck() {
        List<CardType> cards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            cards.add(CardType.DUKE);
            cards.add(CardType.ASSASSIN);
            cards.add(CardType.CAPTAIN);
            cards.add(CardType.AMBASSADOR);
            cards.add(CardType.CONTESSA);
        }
        deck.addAll(cards);
        shuffleDeck();
    }

    public void dealCards(List<Player> players) {
        for (Player player : players) {
            List<CardType> playerCards = player.getCards();
            playerCards.add(deck.pop());
            playerCards.add(deck.pop());
        }
    }

    public void switchPlayerCard(Game game, Player player, CardType cardToRemove) {
        List<CardType> playerCards = player.getCards();
        playerCards.remove(cardToRemove);
        playerCards.add(deck.pop());

        game.logGameMemory(player.getId() + " switches a card" );

    }

    public void exchangePlayerCards(Game game, Player player) {
        List<CardType> playerCards = player.getCards();
        int originalSize = playerCards.size();
        List<CardType> drawn = List.of(deck.pop(), deck.pop());
        List<CardType> cardsToChooseFrom = new ArrayList<>();
        cardsToChooseFrom.addAll(playerCards);
        cardsToChooseFrom.addAll(drawn);

        List<CardType> chosenCardsToKeep = aiChooseCardService.getCardsToExchange(game,player, cardsToChooseFrom, originalSize);

        List<CardType> toReturn = new ArrayList<>(cardsToChooseFrom);
        toReturn.removeAll(chosenCardsToKeep);
        playerCards.clear();
        playerCards.addAll(chosenCardsToKeep);
        for (CardType c : toReturn) {
            addCardToDeck(c); // or addCardToDeck(c) depending on your API
        }
        shuffleDeck();
    }

    public void addExchangeCards(Player player) {
        List<CardType> playerCards = player.getCards();
        playerCards.add(deck.pop());
        playerCards.add(deck.pop());
    }

    public void exchangeHumanCards(Player player, List<CardType> cardsToKeep) {
        List<CardType> playerCards = player.getCards();

        List<CardType> allCards = new ArrayList<>(playerCards);
        List<CardType> toReturn = new ArrayList<>(allCards);
        toReturn.removeAll(cardsToKeep);
        playerCards.clear();
        playerCards.addAll(cardsToKeep);
        for (CardType c : toReturn) {
            addCardToDeck(c); // or addCardToDeck(c)
        }
        shuffleDeck();
    }

    public void shuffleDeck() {
        List<CardType> list = new ArrayList<>(deck);  // copy into list
        Collections.shuffle(list);                    // shuffle list
        deck.clear();                                 // clear deque
        deck.addAll(list);                            // rebuild deque
    }
    public void removeHumanCard(Game game, CardType cardToRemove) {
        List<CardType> humanCards = game.getPlayer("human").getCards();
        humanCards.remove(cardToRemove);
    }
    public boolean removePlayerCard(Game game, Player player) {
        List<CardType> playerCards = player.getCards();
        CardType cardToRemove;
        if(player.getCards().size() == 1){
            cardToRemove = player.getCards().getFirst();
        } else {
            cardToRemove = aiChooseCardService.getCardToLoose(game, player);
        }
        playerCards.remove(cardToRemove);
        addCardToDeck(cardToRemove);
        game.logGameMemory(player.getId() + " looses a card" );
        return PlayerUtil.isPlayerAlive(player);
    }




    private void addCardToDeck(CardType cardToAdd) {
        this.deck.addLast(cardToAdd);
    }


    public Deque<CardType> getDeck() { return deck; }


}
