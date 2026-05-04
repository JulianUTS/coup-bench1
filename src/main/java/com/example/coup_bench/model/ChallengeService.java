package com.example.coup_bench.model;

import com.example.coup_bench.model.Enums.CardType;
import org.springframework.stereotype.Service;


@Service
public class ChallengeService {
    private String blockerId;
    private CardType blockingRole;
    private String challengerId;

    public String getBlockerId() { return blockerId; }
    public void setBlockerId(String id) { this.blockerId = id; }

    public CardType getBlockingRole() { return blockingRole; }
    public void setBlockingRole(CardType role) { this.blockingRole = role; }

    public String getChallengerId() { return challengerId; }
    public void setChallengerId(String id) { this.challengerId = id; }

    public void clearChallengeData(){
        this.challengerId = null;
        this.blockerId = null;
        this.blockingRole = null;
    }

    public void blockDeclared(String blockerId, CardType blockingRole) {
        this.blockerId = blockerId;
        this.blockingRole = blockingRole;

    }

}
