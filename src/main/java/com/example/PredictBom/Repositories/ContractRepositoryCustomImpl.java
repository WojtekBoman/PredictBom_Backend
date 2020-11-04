package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.Player;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

public class ContractRepositoryCustomImpl implements ContractRepositoryCustom {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean update(Contract contract) {
        Query query = new Query(Criteria.where("_id").is(contract.getId()));
        Update update = new Update();
        update.set("offers",contract.getOffers());
        update.set("countOfContracts",contract.getCountOfContracts());
        update.set("modifiedDate",contract.getModifiedDate());
        update.set("predictionMarket",contract.getPredictionMarket());
        UpdateResult result = mongoTemplate.updateFirst(query, update, Contract.class);
        return result.getModifiedCount()>0;
    }
}
