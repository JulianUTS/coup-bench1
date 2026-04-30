package com.example.coup_bench.repo;


import com.example.coup_bench.model.repoModels.AgentLifetimeStats;
import com.example.coup_bench.model.repoModels.GameSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends MongoRepository<AgentLifetimeStats, String> {
}
