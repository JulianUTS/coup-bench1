package com.example.coup_bench;

import com.example.coup_bench.model.Card;
import com.example.coup_bench.model.CardType;
import com.example.coup_bench.model.GameState;
import com.example.coup_bench.model.Player;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoupTestService {

    public CoupTestSnapshot playTestGame() {

        // Create players
        Player p1 = new Player("p1", "Alice");
        Player p2 = new Player("p2", "Bob");

        // Give them cards
        p1.addCard(new Card(CardType.DUKE));
        p1.addCard(new Card(CardType.ASSASSIN));

        p2.addCard(new Card(CardType.CAPTAIN));
        p2.addCard(new Card(CardType.CONTESSA));

        // Simulate a simple action
        p1.addCoins(1);
        String action = "Alice takes income (+1 coin)";

        return new CoupTestSnapshot(
                GameState.IN_PROGRESS,
                List.of(p1, p2),
                "p2",          // Bob's turn next
                action
        );
    }
}
