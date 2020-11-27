package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.ERole;
import com.example.PredictBom.Entities.Player;
import com.example.PredictBom.Entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends MongoRepository<Player,String>,PlayerRepositoryCustom {
    Player findByUsername(String username);
    List<Player> findAll();
    @Query("{'budget' : {$ne : null}}")
    List<Player> findByOrderByBudgetDesc(Pageable pageable);
}
