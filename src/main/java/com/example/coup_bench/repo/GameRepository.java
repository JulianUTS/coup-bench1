package com.example.coup_bench.repo;


import com.example.coup_bench.model.Game;
import com.example.coup_bench.model.GameSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public interface GameRepository extends MongoRepository<GameSummary, String> {
}
