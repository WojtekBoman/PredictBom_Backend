package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Models.OffersToBuyResponse;
import com.example.PredictBom.Repositories.*;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoTransactionException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OfferService {

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

//    backoff = @Backoff(delay = 10), maxAttempts = 10
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    @Retryable( value = MongoCommandException.class,
            maxAttempts = 2, backoff = @Backoff(delay = 100))
    public BuyContractResponse buyShares(String username, int offerId, int countOfShares) throws MongoCommandException {

        Player purchaser = playerRepository.findByUsername(username);

        Optional<SalesOffer> optOffer = salesOfferRepository.findById(offerId);
        if (!optOffer.isPresent()) return BuyContractResponse.builder().info("Wybrana oferta nie istnieje").build();
        SalesOffer offer = optOffer.get();
        System.out.println("Budżet "+purchaser);
        System.out.println("Cenka "+countOfShares * offer.getValueOfShares());
        if (purchaser.getBudget() < countOfShares * offer.getValueOfShares())
            return BuyContractResponse.builder().info("Masz za mało pieniędzy").build();
        if (countOfShares > offer.getCountOfContracts())
            BuyContractResponse.builder().info("W tej ofercie ma tyle akcji").build();
        offer.setCountOfContracts(offer.getCountOfContracts() - countOfShares);
        Optional<Contract> optContract = contractRepository.findById(offer.getContractId());
        if (!optContract.isPresent()) return BuyContractResponse.builder().info("Wystąpił błąd").build();
        Contract contract = optContract.get();
        if(contract.getPlayerId() != null && contract.getPlayerId().equals(username)) return BuyContractResponse.builder().info("Nie możesz kupować własnych akcji !").build();
        Player player = playerRepository.findByUsername(contract.getPlayerId());
        if (player != null) {
            player.setBudget(player.getBudget() + countOfShares * offer.getValueOfShares());
            playerRepository.update(player);
        }
        if (offer.getCountOfContracts() == 0) {
            System.out.println("Przed " + contract.getOffers().size());
            salesOfferRepository.deleteById(offerId);
            contract.deleteOffer(offerId);
            System.out.println("Po " + contract.getOffers().size());
            if (contract.getOffers().size() == 0 && contract.getCountOfContracts() == 0) {
                contractRepository.deleteById(contract.getId());
            } else {
                contractRepository.update(contract);
            }
        } else {
            salesOfferRepository.update(offer);
            contract.updateOffer(offer);
            contractRepository.update(contract);
        }
        Transaction transaction = Transaction.builder().id(counterService.getNextId("transactions"))
                .price(offer.getValueOfShares())
                .dealer(contract.getPlayerId())
                .purchaser(username)
                .bet(contract.getBet())
                .marketInfo(contract.getMarketInfo())
                .option(contract.isContractOption())
                .countOfShares(countOfShares)
                .build();

        transactionRepository.save(transaction);
        purchaser.setBudget(purchaser.getBudget() - countOfShares * offer.getValueOfShares());
        playerRepository.update(purchaser);

        Contract boughtContract = findContractWithSamePrice(username, contract.getBet().getMarketId(), contract.getBet().getId(), contract.isContractOption(), countOfShares);
        return BuyContractResponse.builder().info("Zakupiono akcje").boughtContract(boughtContract).purchaser(purchaser).build();
    }

    private Contract findContractWithSamePrice(String username,int marketId, int betId, boolean option, int buyShares) {
        Optional<Contract> optionalContract = contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId,option);
        Contract contract;
        if(optionalContract.isPresent()){
            System.out.println("No znalazłem coś tam");
            contract = optionalContract.get();
            contract.setCountOfContracts(contract.getCountOfContracts() + buyShares);
            contract.setModifiedDate(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date()));
            contractRepository.update(contract);
        }else{
            Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);
            if(!optMarket.isPresent()) return null;
            PredictionMarket market = optMarket.get();
            Set<Bet> bet = market.getBets().stream().filter(bet1 -> bet1.getId() == betId).collect(Collectors.toSet());
            MarketInfo marketInfo = MarketInfo.builder().topic(market.getTopic()).marketCover(market.getMarketCover()).marketCategory(market.getCategory()).build();
            contract = Contract.builder().id(counterService.getNextId("contracts")).bet(bet.iterator().next()).contractOption(option).countOfContracts(buyShares).marketInfo(marketInfo).playerId(username).build();
            contractRepository.save(contract);
        }
        return contract;
    }
}
