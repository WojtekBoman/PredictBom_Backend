package com.example.PredictBom.Services.HelperInterfaces;

import com.example.PredictBom.Constants.SettingsParams;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Repositories.ContractRepository;
import com.example.PredictBom.Repositories.PredictionMarketRepository;
import com.example.PredictBom.Repositories.TransactionRepository;
import com.example.PredictBom.Services.CounterService;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


public interface BuyingHelper {

    default int checkBuyingLimit(TransactionRepository transactionRepository, String purchaser, int betId, boolean option, int shares) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        String date24hAgo = new SimpleDateFormat(SettingsParams.DATE_FORMAT, Locale.GERMANY).format(cal.getTime());
        List<Transaction> userTransactions = transactionRepository.findAllByPurchaserAndBetIdAndOptionInLast24hours(purchaser,betId,option,date24hAgo);

        return userTransactions.stream().mapToInt(Transaction::getShares).sum();
    }

    default Contract upsertContractWithSamePrice(ContractRepository contractRepository, PredictionMarketRepository predictionMarketRepository, CounterService counterService, String username, int marketId, int betId, boolean option, int buyShares) {
        Optional<Contract> optionalContract = contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId,option);
        Contract contract;
        if(optionalContract.isPresent()){
            contract = optionalContract.get();
            contract.setShares(contract.getShares() + buyShares);
            contract.setModifiedDate(new SimpleDateFormat(SettingsParams.DATE_FORMAT, SettingsParams.LOCALE_PL).format(new Date()));
            contractRepository.update(contract);
        }else{
            Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);
            if(!optMarket.isPresent()) return null;
            PredictionMarket market = optMarket.get();
            Set<Bet> bet = market.getBets().stream().filter(bet1 -> bet1.getId() == betId).collect(Collectors.toSet());
            MarketInfo marketInfo = MarketInfo.builder().topic(market.getTopic()).marketCover(market.getMarketCover()).marketCategory(market.getCategory()).build();
            contract = Contract.builder().id(counterService.getNextId("contracts")).bet(bet.iterator().next()).contractOption(option).shares(buyShares).marketInfo(marketInfo).playerId(username).build();
            contractRepository.save(contract);
        }
        return contract;
    }
}
