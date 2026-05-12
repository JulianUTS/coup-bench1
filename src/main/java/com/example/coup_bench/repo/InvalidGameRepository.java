package com.example.coup_bench.repo;


import com.example.coup_bench.model.repoModels.GameSummary;
import com.example.coup_bench.model.repoModels.InvalidGameSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidGameRepository extends MongoRepository<InvalidGameSummary, String> {
}
