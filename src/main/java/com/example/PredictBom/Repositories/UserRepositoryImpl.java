package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.User;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class UserRepositoryImpl implements UserRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean update(User user) {
        Query query = new Query(Criteria.where("_id").is(user.getUsername()));
        Update update = new Update();
        update.set("password",user.getPassword());
        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);
        return result.getModifiedCount()>0;
    }
}
