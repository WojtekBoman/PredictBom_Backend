package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Offer;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class SalesOfferRepositoryCustomImpl implements SalesOfferRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean update(Offer salesOffer) {

        Query query = new Query(Criteria.where("_id").is(salesOffer.getId()));
        Update update = new Update();
        update.set("shares",salesOffer.getShares());
        UpdateResult result = mongoTemplate.updateFirst(query, update, Offer.class);
        return result.getModifiedCount()>0;
    }
}
