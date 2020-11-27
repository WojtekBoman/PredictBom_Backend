package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CounterRepository extends MongoRepository<Counter,String> {

    Counter findByName(String name);
}
