package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Transaction;
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
    List<Transaction> findAllByDealer(String username, Sort sort);
    List<Transaction> findAllByDealerAndOption(String username, boolean option, Sort sort);
    List<Transaction> findAllByPurchaser(String username, Sort sort);
    List<Transaction> findAllByPurchaserAndOption(String username, boolean option, Sort sort);
    @Query("{'bet.id' : ?0, 'option' : ?1}" )
    List<Transaction> findAllByBetIdAndOption(int betId, boolean option);
}


