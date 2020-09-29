package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.PredictionMarket;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class PredictionMarketRepositoryCustomImpl implements PredictionMarketRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean update(PredictionMarket predictionMarket) {
        Query query = new Query(Criteria.where("_id").is(predictionMarket.getMarketId()));
        Update update = new Update();
        update.set("bets",predictionMarket.getBets());
        UpdateResult result = mongoTemplate.updateFirst(query, update, PredictionMarket.class);
        return result.getModifiedCount()>0;
    }
}
