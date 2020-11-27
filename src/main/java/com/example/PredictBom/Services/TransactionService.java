package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.Transaction;
import com.example.PredictBom.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    public List<Transaction> getTransactions(int betId, boolean chosenOption,String timeAgo) {

        return transactionRepository.findTransactionsToChart(betId,chosenOption,timeAgo, Sort.by(Sort.Direction.ASC,"transactionDate"));
    }

    public List<Transaction> getPurchaserTransactionsAndOption(String username,boolean option,String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {
        List<Transaction> transactions = transactionRepository.findAllByPurchaserAndOption(username,option, Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));

        return filterTransactions(betTitle, marketTitle, marketCategory, transactions);
    }

    public List<Transaction> getPurchaserTransactions(String username,String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection){
        System.out.println(sortAttribute + sortDirection);
        List<Transaction> transactions = transactionRepository.findAllByPurchaser(username, Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        System.out.println(transactions);
        return filterTransactions(betTitle,marketTitle,marketCategory,transactions);
    }



    public List<Transaction> getDealerTransactions(String username,String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection){
        System.out.println(sortAttribute + sortDirection);
        List<Transaction> transactions = transactionRepository.findAllByDealer(username, Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        System.out.println(transactions);
        return filterTransactions(betTitle,marketTitle,marketCategory,transactions);
    }

    public List<Transaction> getDealerTransactionsByOption(String username,boolean option, String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection){
        System.out.println(sortAttribute + sortDirection);
        List<Transaction> transactions = transactionRepository.findAllByDealerAndOption(username,option,Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        System.out.println(transactions);

        return filterTransactions(betTitle,marketTitle,marketCategory,transactions);
    }

    private List<Transaction> filterTransactions(String betTitle, String marketTitle, String[] marketCategory, List<Transaction> transactions) {
        List<Transaction> marketsFilteredByTitle = transactions.stream().filter(item -> item.getMarketInfo().getTopic().toLowerCase().contains(marketTitle.toLowerCase())).collect(Collectors.toList());
        if(marketsFilteredByTitle.size() == 0) return marketsFilteredByTitle;
        List<Transaction> contracts = marketsFilteredByTitle.stream().filter(item -> item.getBet().getChosenOption().toLowerCase().contains(betTitle.toLowerCase())).collect(Collectors.toList());
        if(marketCategory.length == 0) return contracts;
        List<Transaction> filteredMarkets = new ArrayList<>();
        for(String market : marketCategory) {
            filteredMarkets.addAll(contracts.stream().filter(item -> item.getMarketInfo().getMarketCategory().toString().toLowerCase().contains(market.toLowerCase())).collect(Collectors.toList()));
        }
        return filteredMarkets;
    }
}
