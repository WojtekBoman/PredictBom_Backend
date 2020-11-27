package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Moderator;
import com.example.PredictBom.Entities.PredictionMarket;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PredictionMarketRepository extends MongoRepository<PredictionMarket,String>, PredictionMarketRepositoryCustom {

    List<PredictionMarket> findAll();

    List<PredictionMarket> findByBetsIsNullAndAuthor(String author, Sort sort);

    List<PredictionMarket> findByPublishedFalse(String author, Sort sort);

    List<PredictionMarket> findByPublishedTrueAndAuthor(String author,Sort sort);

    Optional<PredictionMarket> findByMarketId(Integer marketId);

    Optional<PredictionMarket> findByTopic(String topic);

    List<PredictionMarket> findByPublishedTrue(Sort sort);

    @Query("{'correctBetId' : {$lt : 1},'published': {$eq : true}}")
    List<PredictionMarket> findPublishedNotSolvedMarkets(Sort sort);

    @Query("{'correctBetId' : {$gt : 0}}")
    List<PredictionMarket> findSolvedMarkets(Sort sort);


}
