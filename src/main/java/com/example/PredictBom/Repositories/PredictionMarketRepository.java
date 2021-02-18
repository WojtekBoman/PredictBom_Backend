package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.PredictionMarket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PredictionMarketRepository extends MongoRepository<PredictionMarket,String>, PredictionMarketRepositoryCustom {

    List<PredictionMarket> findAll();

    Optional<PredictionMarket> findByMarketId(Integer marketId);

    Optional<PredictionMarket> findByTopic(String topic);

    void deleteByMarketId(int marketId);

    @Query("{'author' : ?0,'bets': {$eq : null}, 'topic':{$regex: ?1, $options: 'i'}, 'category':{$in: ?2}}")
    Page<PredictionMarket> findWaitingForBetsModMarkets(String author,String topic, List<String> category, Pageable pageable);

    @Query("{'author' : ?0,'bets': {$eq : null}, 'topic':{$regex: ?1, $options: 'i'}}")
    Page<PredictionMarket> findWaitingForBetsModMarkets(String author,String topic, Pageable pageable);

    @Query("{'author' : ?0,'correctBetId' : {$lt : 1},'published': {$eq : true}, 'topic':{$regex: ?1, $options: 'i'}, 'category':{$in: ?2}}")
    Page<PredictionMarket> findPublicModMarkets(String author,String topic, List<String> category, Pageable pageable);

    @Query("{'author' : ?0,'correctBetId' : {$lt : 1},'published': {$eq : true}, 'topic':{$regex: ?1, $options: 'i'}}")
    Page<PredictionMarket> findPublicModMarkets(String author,String topic, Pageable pageable);

    @Query("{'author' : ?0,'correctBetId' : {$gt : 0},'published': {$eq : true}, 'topic':{$regex: ?1, $options: 'i'}, 'category':{$in: ?2}}")
    Page<PredictionMarket> findSolvedModMarkets(String author,String topic, List<String> category, Pageable pageable);

    @Query("{'author' : ?0,'correctBetId' : {$gt : 0},'published': {$eq : true}, 'topic':{$regex: ?1, $options: 'i'}}")
    Page<PredictionMarket> findSolvedModMarkets(String author,String topic, Pageable pageable);

    @Query("{'author' : ?0,'published': {$eq : false}, 'topic':{$regex: ?1, $options: 'i'}, 'category':{$in: ?2}}")
    Page<PredictionMarket> findPrivateModMarkets(String author,String topic, List<String> category, Pageable pageable);

    @Query("{'author' : ?0,'published': {$eq : false}, 'topic':{$regex: ?1, $options: 'i'}}")
    Page<PredictionMarket> findPrivateModMarkets(String author,String topic, Pageable pageable);

    @Query("{'correctBetId' : {$lt : 1},'published': {$eq : true}, 'topic':{$regex: ?0, $options: 'i'}, 'category':{$in: ?1}}")
    Page<PredictionMarket> findPublishedNotSolvedMarkets(String topic, List<String> category, Pageable pageable);

    @Query("{'correctBetId' : {$lt : 1},'published': {$eq : true},'topic':{$regex: ?0, $options: 'i'}}")
    Page<PredictionMarket> findPublishedNotSolvedMarkets(String topic,Pageable pageable);

    @Query("{'correctBetId' : {$gt : 0},'topic':{$regex: ?0, $options: 'i'}}")
    Page<PredictionMarket> findSolvedMarkets(String topic,Pageable pageable);

    @Query("{'correctBetId' : {$gt : 0},'topic':{$regex: ?0, $options: 'i'}, 'category':{$in: ?1}}")
    Page<PredictionMarket> findSolvedMarkets(String topic,List<String> category,Pageable pageable);


}
