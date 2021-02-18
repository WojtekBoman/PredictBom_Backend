package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PlayerRepository extends MongoRepository<Player,String>,PlayerRepositoryCustom {
    Player findByUsername(String username);
    List<Player> findAll();
    @Query("{'budget' : {$ne : null}}")
    List<Player> findByOrderByBudgetDesc(Pageable pageable);
}
