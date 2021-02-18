package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, Spring> {

    Optional<Transaction> findFirstByBetIdAndOptionOrderByTransactionDateDesc(int betId, boolean option);
    @Query("{'bet.id' : ?0, 'option' : ?1, 'transactionDate': {$gt : ?2}}" )
    List<Transaction> findTransactionsToChart(int betId, boolean option,String date, Sort sort);
    @Query("{'purchaser' : ?0 ,'bet.id' : ?1, 'option' : ?2, 'transactionDate': {$gt : ?3}}" )
    List<Transaction> findAllByPurchaserAndBetIdAndOptionInLast24hours(String purchaser, int betId, boolean option, String transactionDate);
    @Query("{'purchaser' : ?0 ,'option' : ?1, 'bet.title' : {$regex: ?2}, 'marketInfo.topic' : {$regex: ?3, $options: 'i'}, 'marketInfo.marketCategory':{$in : ?4}}")
    Page<Transaction> findAllByPurchaserAndOption(String username, boolean option, String betTitle, String marketTitle, List<String> marketCategory, Pageable pageable);
    @Query("{'purchaser' : ?0 ,'option' : ?1, 'bet.title' : {$regex: ?2}, 'marketInfo.topic' : {$regex: ?3, $options: 'i'}}" )
    Page<Transaction> findAllByPurchaserAndOption(String username, boolean option, String betTitle, String marketTitle, Pageable pageable);
    @Query("{'purchaser' : ?0,'bet.title' : {$regex: ?1}, 'marketInfo.topic' : {$regex: ?2, $options: 'i'}, 'marketInfo.marketCategory':{$in : ?3}}")
    Page<Transaction> findAllByPurchaser(String username, String betTitle, String marketTitle, List<String> marketCategory, Pageable pageable);
    @Query("{'purchaser' : ?0 , 'bet.title' : {$regex: ?1}, 'marketInfo.topic' : {$regex: ?2, $options: 'i'}}" )
    Page<Transaction> findAllByPurchaser(String username, String betTitle, String marketTitle, Pageable pageable);
    @Query("{'dealer' : ?0 ,'option' : ?1, 'bet.title' : {$regex: ?2}, 'marketInfo.topic' : {$regex: ?3, $options: 'i'}, 'marketInfo.marketCategory':{$in : ?4}}")
    Page<Transaction> findAllByDealerAndOption(String username, boolean option, String betTitle, String marketTitle, List<String> marketCategory, Pageable pageable);
    @Query("{'dealer' : ?0 ,'option' : ?1, 'bet.title' : {$regex: ?2}, 'marketInfo.topic' : {$regex: ?3, $options: 'i'}}" )
    Page<Transaction> findAllByDealerAndOption(String username, boolean option, String betTitle, String marketTitle, Pageable pageable);
    @Query("{'dealer' : ?0,'bet.title' : {$regex: ?1}, 'marketInfo.topic' : {$regex: ?2, $options: 'i'}, 'marketInfo.marketCategory':{$in : ?3}}")
    Page<Transaction> findAllByDealer(String username, String betTitle, String marketTitle, List<String> marketCategory, Pageable pageable);
    @Query("{'dealer' : ?0 , 'bet.title' : {$regex: ?1}, 'marketInfo.topic' : {$regex: ?2, $options: 'i'}}" )
    Page<Transaction> findAllByDealer(String username, String betTitle, String marketTitle, Pageable pageable);

}


