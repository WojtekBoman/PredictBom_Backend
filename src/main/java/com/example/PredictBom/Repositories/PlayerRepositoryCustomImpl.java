package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Player;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class PlayerRepositoryCustomImpl implements PlayerRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean update(Player player) {
        Query query = new Query(Criteria.where("_id").is(player.getUsername()));
        Update update = new Update();
        update.set("budget",player.getBudget());
        update.set("lastLoginDate",player.getLastLoginDate());
        UpdateResult result = mongoTemplate.updateFirst(query, update, Player.class);
        return result.getModifiedCount()>0;
    }


}
