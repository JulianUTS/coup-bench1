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
        Collections.shuffle(cards);
        deck.addAll(cards);
    }

    public void dealCards(List<Player> players) {
        for (Player p : players) {
            addCardToPlayer(p,removeCardFromDeck());
            addCardToPlayer(p,removeCardFromDeck());
        }
    }

    public void switchPlayerCard(Game game, Player player, CardType cardToRemove) {
        removeCardFromPlayer(player, cardToRemove);
        addCardToPlayer(player,removeCardFromDeck());

        game.logGameMemory(player.getId() + " switches a card" );

    }

    public void exchangePlayerCards(Player player) {
        List<CardType> playerCards = player.getCards();
        int cardsToAdd = playerCards.size();
        for(int i = 0; i < cardsToAdd; i++){
            addCardToDeck(playerCards.removeFirst());
            addCardToPlayer(player,removeCardFromDeck());
        }
    }
    public boolean removePlayerCard(Game game, Player player) {
        CardType cardToRemove;
        if(player.getCards().size() == 1){
            cardToRemove = player.getCards().getFirst();
        } else {
            cardToRemove = aiChooseCardService.getCardToLoose(game, player);
        }
        removeCardFromPlayer(player, cardToRemove);
        addCardToDeck(cardToRemove);
        game.logGameMemory(player.getId() + " looses a card" );
        return PlayerUtil.isPlayerAlive(player);
    }

    private void addCardToPlayer(Player player,CardType card) {
        List<CardType> playerCards = player.getCards();
        playerCards.add(card); }

    private void removeCardFromPlayer(Player player, CardType cardToRemove) {
        List<CardType> playerCards = player.getCards();
        for (CardType c : playerCards) {
            if (c.equals(cardToRemove)) {
                playerCards.remove(c);
                return;
            }
        }
    }

    private void addCardToDeck(CardType cardToAdd) {
        this.deck.addLast(cardToAdd);
    }

    private CardType removeCardFromDeck() { return this.deck.pop(); }

    public Deque<CardType> getDeck() { return deck; }


}
