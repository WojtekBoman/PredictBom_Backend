package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.*;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, Spring> {
    Optional<Transaction> findFirstByBetIdAndAndOptionOrderByTransactionDateDesc(int betId, boolean option);
}
