package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.PredictionMarket;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PredictionMarketRepositoryCustom  {

    boolean update(PredictionMarket predictionMarket);
}
