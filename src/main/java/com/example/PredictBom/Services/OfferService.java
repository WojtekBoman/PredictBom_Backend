package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Repositories.*;
import com.mongodb.MongoCommandException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OfferService {

    public static final String BOUGHT_SHARES_INFO = "Zakupiono akcje";
    public static final String OFFER_IS_NOT_FOUND_INFO = "Wybrana oferta nie istnieje";
    public static final String NOT_ENOUGH_MONEY_INFO = "Masz za mało pieniędzy";
    public static final String BOUGHT_OWN_OFFERS_INFO = "Nie możesz kupować własnych akcji";
    public static final String NOT_ENOUGH_SHARES_INFO = "Ta oferta nie zawiera tylu akcji";

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PredictionMarketRepository predictionMarketRepository;

    @Autowired
    CounterService counterService;

    @Autowired
    SalesOfferRepository salesOfferRepository;

    @Autowired
    TransactionRepository transactionRepository;

    public String returnLimitInfo(int sharesToBuy) {
        return "Przekroczyłeś dzienny limit zakupów akcji dla tej opcji zakładu. Możesz kupić "+ (sharesToBuy) +" akcji";
    }
    
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    @Retryable( value = MongoCommandException.class,
            maxAttempts = 2, backoff = @Backoff(delay = 100))
    public BuyContractResponse buyShares(String username, int offerId, int shares) throws MongoCommandException {

        Player purchaser = playerRepository.findByUsername(username);

        Optional<Offer> optOffer = salesOfferRepository.findById(offerId);
        if (!optOffer.isPresent()) return BuyContractResponse.builder().info(OFFER_IS_NOT_FOUND_INFO).build();

        Offer offer = optOffer.get();
        if (purchaser.getBudget() < shares * offer.getPrice()) return BuyContractResponse.builder().info(NOT_ENOUGH_MONEY_INFO).build();
        if (shares > offer.getShares()) return BuyContractResponse.builder().info(NOT_ENOUGH_SHARES_INFO).build();
        offer.setShares(offer.getShares() - shares);
        Optional<Contract> optContract = contractRepository.findById(offer.getContractId());
        if (!optContract.isPresent()) return BuyContractResponse.builder().info(OFFER_IS_NOT_FOUND_INFO).build();
        Contract contract = optContract.get();
        if(contract.getPlayerId() != null && contract.getPlayerId().equals(username)) return BuyContractResponse.builder().info(BOUGHT_OWN_OFFERS_INFO).build();

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        String date24hAgo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
        List<Transaction> userTransactions = transactionRepository.findAllByPurchaserAndBetIdAndOptionInLast24hours(username,contract.getBet().getId(),contract.isContractOption(),date24hAgo);
        int sumShares = userTransactions.stream().mapToInt(Transaction::getShares).sum();
        if(sumShares + shares > 1000) return BuyContractResponse.builder().info(returnLimitInfo(1000-sumShares)).build();

        Player player = playerRepository.findByUsername(contract.getPlayerId());
        if (player != null) {
            player.setBudget(player.getBudget() + shares * offer.getPrice());
            playerRepository.update(player);
        }
        if (offer.getShares() == 0) {
            salesOfferRepository.deleteById(offerId);
            contract.deleteOffer(offerId);
            if (contract.getOffers() == null && contract.getShares() == 0) {
                contractRepository.deleteById(contract.getId());
            } else {
                contractRepository.update(contract);
            }
        } else {
            salesOfferRepository.update(offer);
            contract.updateOffer(offer);
            contractRepository.update(contract);
        }
        Transaction transaction = Transaction.builder()
                .id(counterService.getNextId("transactions"))
                .price(offer.getPrice())
                .dealer(contract.getPlayerId())
                .purchaser(username)
                .bet(contract.getBet())
                .marketInfo(contract.getMarketInfo())
                .option(contract.isContractOption())
                .shares(shares)
                .build();


        transactionRepository.save(transaction);
        purchaser.setBudget(purchaser.getBudget() - shares * offer.getPrice());
        playerRepository.update(purchaser);

        Contract boughtContract = findContractWithSamePrice(username, contract.getBet().getMarketId(), contract.getBet().getId(), contract.isContractOption(), shares);
        return BuyContractResponse.builder().info(BOUGHT_SHARES_INFO).boughtContract(boughtContract).purchaser(purchaser).build();
    }

    private Contract findContractWithSamePrice(String username,int marketId, int betId, boolean option, int buyShares) {
        Optional<Contract> optionalContract = contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId,option);
        Contract contract;
        if(optionalContract.isPresent()){
            contract = optionalContract.get();
            contract.setShares(contract.getShares() + buyShares);
            contract.setModifiedDate(new SimpleDateFormat("yyyy-MM-dd    HH:mm:ss").format(new Date()));
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
