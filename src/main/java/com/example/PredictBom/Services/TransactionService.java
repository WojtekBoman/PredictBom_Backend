package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.Transaction;
import com.example.PredictBom.Repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> getTransactions(int betId, boolean chosenOption,String timeAgo) {
        return transactionRepository.findTransactionsToChart
                (
                        betId,
                        chosenOption,
                        timeAgo,
                        Sort.by(Sort.Direction.ASC,"transactionDate")
                );
    }

    public Page<Transaction> getPurchaserTransactionsAndOption(String username, boolean option, String betTitle, String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection) {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        if(marketCategory.length == 0) {
            return transactionRepository.findAllByPurchaserAndOption(username,option, betTitle, marketTitle,pageRequest);
        }
        return transactionRepository.findAllByPurchaserAndOption(username,option, betTitle, marketTitle,Arrays.asList(marketCategory),pageRequest);
    }

    public Page<Transaction> getPurchaserTransactions(String username,String betTitle, String marketTitle, String[] marketCategory,Pageable pageable, String sortAttribute, String sortDirection){
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        if(marketCategory.length == 0) {
            return transactionRepository.findAllByPurchaser(username, betTitle, marketTitle,pageRequest);
        }
        return transactionRepository.findAllByPurchaser(username, betTitle, marketTitle,Arrays.asList(marketCategory),pageRequest);
    }


    public Page<Transaction> getDealerTransactions(String username,String betTitle, String marketTitle, String[] marketCategory,Pageable pageable, String sortAttribute, String sortDirection){
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        if(marketCategory.length == 0) {
            return transactionRepository.findAllByDealer(username, betTitle, marketTitle,pageRequest);
        }
        return transactionRepository.findAllByDealer(username, betTitle, marketTitle,Arrays.asList(marketCategory),pageRequest);
    }

    public Page<Transaction> getDealerTransactionsByOption(String username,boolean option, String betTitle, String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection){
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        if(marketCategory.length == 0) {
            return transactionRepository.findAllByDealerAndOption(username,option, betTitle, marketTitle,pageRequest);
        }
        return transactionRepository.findAllByDealerAndOption(username,option, betTitle, marketTitle,Arrays.asList(marketCategory),pageRequest);
    }

}
