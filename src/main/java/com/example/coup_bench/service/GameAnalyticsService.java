package com.example.coup_bench.service;

import com.example.coup_bench.model.ActionRecord;
import com.example.coup_bench.model.repoModels.InteractionRecord;
import com.example.coup_bench.model.repoModels.TurnSnapshot;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class GameAnalyticsService {
    private final List<ActionRecord> bluffLog= new ArrayList<>();
    private final List<InteractionRecord> interactionLog= new ArrayList<>();
    private final List<TurnSnapshot> turnSnapshotLog= new ArrayList<>();

    public List<TurnSnapshot> getTurnSnapshotLog() {
        return turnSnapshotLog;
    }

    public List<InteractionRecord> getInteractionLog() {
        return interactionLog;
    }
    public void logTurnSnapshot(TurnSnapshot turnSnapshot) {
        this.turnSnapshotLog.add(turnSnapshot);
    }
    public void logInteraction(InteractionRecord interactionRecord) {
        this.interactionLog.add(interactionRecord);
    }

    public List<ActionRecord> getBluffLog() {
        return this.bluffLog;
    }

    public void logBluff(ActionRecord record){
        this.bluffLog.add(record);
    }

}
