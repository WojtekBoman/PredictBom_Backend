package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Bet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BetRepository extends MongoRepository<Bet,String> {
    Optional<Bet> findById(int betId);
    Bet deleteBetById(int betId);
}
