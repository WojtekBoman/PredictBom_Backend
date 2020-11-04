package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlayerRepository extends MongoRepository<Player,String>,PlayerRepositoryCustom {
    Player findByUsername(String username);
}
