package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, Spring> {
    Optional<Transaction> findFirstByBetIdAndAndOptionOrderByTransactionDateDesc(int betId, boolean option);

    @Query("{'bet.id' : ?0, 'option' : ?1, 'transactionDate': {$gt : ?2}}" )
    List<Transaction> findTransactionsToChart(int betId, boolean option,String date, Sort sort);
    List<Transaction> findAllByDealer(String username, Sort sort);
    List<Transaction> findAllByDealerAndOption(String username, boolean option, Sort sort);
    List<Transaction> findAllByPurchaser(String username, Sort sort);
    List<Transaction> findAllByPurchaserAndOption(String username, boolean option, Sort sort);
}
