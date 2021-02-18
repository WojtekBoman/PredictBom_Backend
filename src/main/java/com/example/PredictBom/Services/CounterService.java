package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
public class CounterService {

    private final MongoOperations mongo;

    public int getCurrentValue(String collectionName) {
        Counter counter = mongo.findOne(query(where("_id").is(collectionName)), Counter.class);
        if (counter != null) return counter.getValue();

        return 0;
    }

    public void updateValue(String collectionName, int newValue) {
        mongo.findAndModify(query(where("_id").is(collectionName)),
                new Update().set("value", newValue),
                options().returnNew(true),
                Counter.class);
    }

    public int getNextId(String collectionName) {
        Counter counter = mongo.findAndModify(query(where("_id").is(collectionName)),
                new Update().inc("value", 1),
                options().returnNew(true),
                Counter.class);

        return !Objects.isNull(counter) ? counter.getValue() : 1;
    }
}
