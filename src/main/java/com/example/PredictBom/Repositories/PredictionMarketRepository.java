package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Moderator;
import com.example.PredictBom.Entities.PredictionMarket;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PredictionMarketRepository extends MongoRepository<PredictionMarket,String>, PredictionMarketRepositoryCustom {

    List<PredictionMarket> findAll();

    List<PredictionMarket> findByBetsIsNullAndAuthor(String author, Sort sort);

    List<PredictionMarket> findByPublishedFalse(String author, Sort sort);

    Optional<PredictionMarket> findByMarketId(Integer marketId);

    Optional<PredictionMarket> findByTopic(String topic);

}
