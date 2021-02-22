package com.example.PredictBom.Services;

import com.example.PredictBom.Constants.MarketConstants;
import com.example.PredictBom.Constants.SettingsParams;
import com.example.PredictBom.Models.BetRequest;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BetPrice;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Models.MarketWithBetsPricesResponse;
import com.example.PredictBom.Repositories.*;
import com.mongodb.MongoCommandException;
import lombok.RequiredArgsConstructor;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionMarketService implements BuyingHelper {

    private final UserRepository userRepository;

    private final BetRepository betRepository;

    private final CounterService counterService;

    private final SalesOfferRepository salesOfferRepository;

    private final ContractRepository contractRepository;

    private final PredictionMarketRepository predictionMarketRepository;

    private final TransactionRepository transactionRepository;

    private final PlayerRepository playerRepository;

    public ResponseEntity<?> createPredictionMarket(String username, String topic, String category, String endDate, String description) throws IllegalArgumentException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        Optional<PredictionMarket> marketOptional = predictionMarketRepository.findByTopic(topic);

        if (!userOptional.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.UNLOGGED_USER_CREATING_MARKET_INFO);
        if (marketOptional.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.MARKET_EXISTING_INFO);
        MarketCategory marketCategory = MarketCategory.valueOf(category);

        if (endDate.length() == 0) endDate = "3000-01-01";

        PredictionMarket predictionMarket = PredictionMarket
                .builder()
                .marketId(counterService.getNextId("markets"))
                .topic(topic)
                .category(marketCategory)
                .author(username)
                .endDate(endDate)
                .description(description)
                .build();
        predictionMarketRepository.save(predictionMarket);


        return ResponseEntity.ok(predictionMarket);
    }

    public ResponseEntity<?> editMarket(int marketId, String topic, String category, String endDate, String description) {
        Optional<PredictionMarket> marketOptional = predictionMarketRepository.findByMarketId(marketId);
        if (!marketOptional.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_MARKET_INFO);

        PredictionMarket marketToEdit = marketOptional.get();

        marketToEdit.setTopic(topic);
        marketToEdit.setEndDate(endDate);
        marketToEdit.setDescription(description);
        MarketCategory marketCategory = MarketCategory.valueOf(category);

        marketToEdit.setCategory(marketCategory);
        if (marketToEdit.getBets() != null) {
            List<Contract> contracts = contractRepository.findAllByMarketId(marketId);
            for (Contract contract : contracts) {
                MarketInfo marketInfo = contract.getMarketInfo();
                marketInfo.setTopic(topic);
                marketInfo.setMarketCategory(marketCategory);
                contractRepository.update(contract);
            }
        }
        predictionMarketRepository.update(marketToEdit);
        return ResponseEntity.ok(marketToEdit);
    }

    @Transactional
    public ResponseEntity<?> addBet(BetRequest betRequest) {

        Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(betRequest.getMarketId());

        if (!optionalPredictionMarket.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_MARKET_INFO);

        PredictionMarket predictionMarket = optionalPredictionMarket.get();

        if (predictionMarket.getBets() != null && predictionMarket.getBets().stream().anyMatch(bet -> bet.getTitle().equals(betRequest.getTitle())))
            return ResponseEntity.badRequest().body(MarketConstants.BET_EXISTING_INFO);

        Bet newBet = Bet
                .builder()
                .id(counterService.getNextId("bets"))
                .marketId(betRequest.getMarketId())
                .title(betRequest.getTitle())
                .build();

        predictionMarket.addBet(newBet);
        predictionMarketRepository.update(predictionMarket);
        betRepository.save(newBet);


        MarketInfo marketInfo = MarketInfo.builder().marketCategory(predictionMarket.getCategory()).marketCover(predictionMarket.getMarketCover()).topic(predictionMarket.getTopic()).build();
        Contract contractTrue = Contract.builder().id(counterService.getNextId("contracts")).bet(newBet).shares(0).contractOption(true).marketInfo(marketInfo).build();
        Contract contractFalse = Contract.builder().id(counterService.getNextId("contracts")).bet(newBet).shares(0).contractOption(false).marketInfo(marketInfo).build();
        Offer offerTrue = Offer.builder().id(counterService.getNextId("offers")).contractId(contractTrue.getId()).shares(betRequest.getShares()).price(betRequest.getYesPrice()).build();
        Offer offerFalse = Offer.builder().id(counterService.getNextId("offers")).contractId(contractFalse.getId()).shares(betRequest.getShares()).price(betRequest.getNoPrice()).build();
        contractTrue.addOffer(offerTrue);
        contractFalse.addOffer(offerFalse);
        List<Contract> contractsToSave = new ArrayList<>(Arrays.asList(contractFalse, contractTrue));
        contractRepository.saveAll(contractsToSave);
        List<Offer> offersToSave = new ArrayList<>(Arrays.asList(offerFalse, offerTrue));
        salesOfferRepository.saveAll(offersToSave);


        BetPrice betPrice = BetPrice.builder().betId(newBet.getId()).yesPrice(betRequest.getYesPrice()).noPrice(betRequest.getNoPrice()).build();
        return ResponseEntity.ok(MarketWithBetsPricesResponse.builder().predictionMarket(predictionMarket).betPrice(betPrice).build());

    }

    @Transactional
    public ResponseEntity<?> deleteBet(int betId) {

        Optional<Bet> optionalBet = betRepository.findById(betId);
        if (!optionalBet.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_BET_INFO);

        Bet bet = optionalBet.get();

        Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(bet.getMarketId());
        if (!optionalPredictionMarket.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_MARKET_INFO);

        PredictionMarket marketToDeleteBet = optionalPredictionMarket.get();

        marketToDeleteBet.deleteBet(betId);
        if (marketToDeleteBet.getBets().size() == 0) marketToDeleteBet.setBets(null);
        predictionMarketRepository.update(marketToDeleteBet);
        betRepository.deleteBetById(betId);
        List<Contract> contractList = contractRepository.deleteByBetId(betId);
        for (Contract contract : contractList) {
            salesOfferRepository.deleteByContractId(contract.getId());
        }

        return ResponseEntity.ok(marketToDeleteBet);
    }

    //GET METHODS

    //METHOD TO GET PRIVATE MARKETS WITHOUT BETS WHERE AUTHOR IS CURRENTLY LOGGED MODERATOR
    public ResponseEntity<?> getFilteredMarketsWaitingForBets(String username, String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection) {

        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.fromString(sortDirection), sortAttribute));
        if (marketCategory.length == 0) {
            return ResponseEntity.ok(predictionMarketRepository.findWaitingForBetsModMarkets(username, marketTitle, pageRequest));
        }
        return ResponseEntity.ok(predictionMarketRepository.findWaitingForBetsModMarkets(username, marketTitle, Arrays.asList(marketCategory), pageRequest));
    }

    //METHOD TO GET PUBLIC MARKETS WHERE AUTHOR IS LOGGED MODERATOR
    public ResponseEntity<?> getPublicModMarkets(String username, String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection) {

        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.fromString(sortDirection), sortAttribute));
        if (marketCategory.length == 0) {
            return ResponseEntity.ok(predictionMarketRepository.findPublicModMarkets(username, marketTitle, pageRequest));
        }
        return ResponseEntity.ok(predictionMarketRepository.findPublicModMarkets(username, marketTitle, Arrays.asList(marketCategory), pageRequest));
    }

    public ResponseEntity<?> getFilteredPrivateMarkets(String username, String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection) {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.fromString(sortDirection), sortAttribute));
        if (marketCategory.length == 0) {
            return ResponseEntity.ok(predictionMarketRepository.findPrivateModMarkets(username, marketTitle, pageRequest));
        }
        return ResponseEntity.ok(predictionMarketRepository.findPrivateModMarkets(username, marketTitle, Arrays.asList(marketCategory), pageRequest));
    }


    public Page<PredictionMarket> getPublicMarkets(String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection) {

        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.fromString(sortDirection), sortAttribute));
        if (marketCategory.length == 0) {
            return predictionMarketRepository.findPublishedNotSolvedMarkets(marketTitle, pageRequest);
        }
        return (predictionMarketRepository.findPublishedNotSolvedMarkets(marketTitle, Arrays.asList(marketCategory), pageRequest));
    }

    public Page<PredictionMarket> getSolvedModMarkets(String username, String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection) {

        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.fromString(sortDirection), sortAttribute));
        if (marketCategory.length == 0) {
            return predictionMarketRepository.findSolvedModMarkets(username, marketTitle, pageRequest);
        }
        return (predictionMarketRepository.findSolvedModMarkets(username, marketTitle, Arrays.asList(marketCategory), pageRequest));
    }

    public Page<PredictionMarket> getSolvedMarkets(String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection) {

        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.fromString(sortDirection), sortAttribute));
        if (marketCategory.length == 0) {
            return predictionMarketRepository.findSolvedMarkets(marketTitle, pageRequest);
        }
        return (predictionMarketRepository.findSolvedMarkets(marketTitle, Arrays.asList(marketCategory), pageRequest));
    }

    public ResponseEntity<?> getMarketById(int id) {
        Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);
        if (optionalPredictionMarket.isPresent()) {
            return ResponseEntity.ok(optionalPredictionMarket.get());
        }
        return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_MARKET_INFO);
    }

    @Transactional
    public ResponseEntity<?> setMarketCover(int id, MultipartFile marketCover) throws IOException {

        Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);

        if (!optionalPredictionMarket.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_MARKET_INFO);
        PredictionMarket predictionMarket = optionalPredictionMarket.get();
        if (predictionMarket.isPublished()) return ResponseEntity.badRequest().body(MarketConstants.CANNOT_CHANGED_COVER_INFO);
        predictionMarket.setMarketCover(new Binary(BsonBinarySubType.BINARY, marketCover.getBytes()));
        //if market contains contracts, change cover in them too
        if (predictionMarket.getBets() != null) {
            List<Contract> contracts = contractRepository.findAllByMarketId(id);
            for (Contract contract : contracts) {
                MarketInfo marketInfo = contract.getMarketInfo();
                marketInfo.setMarketCover(new Binary(BsonBinarySubType.BINARY, marketCover.getBytes()));
                contract.setMarketInfo(marketInfo);
                contractRepository.update(contract);
            }
        }
        predictionMarketRepository.update(predictionMarket);


        return ResponseEntity.ok(predictionMarket);
    }

    public ResponseEntity<?> deleteMarket(int marketId) {

        Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);
        if (!optMarket.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_MARKET_INFO);

        PredictionMarket market = optMarket.get();
        if (market.getBets() != null) return ResponseEntity.badRequest().body(MarketConstants.FIRST_DELETE_BET_INFO);

        predictionMarketRepository.deleteByMarketId(marketId);

        return ResponseEntity.ok(market);
    }


    public BetPrice getPrice(int betId) {

        double yesPrice, noPrice;
        yesPrice = checkLastPrice(betId, true);
        noPrice = checkLastPrice(betId, false);

        return BetPrice.builder().betId(betId).yesPrice(yesPrice).noPrice(noPrice).build();

    }

    public ResponseEntity<?> getLastPrice(int betId, boolean option) {

        double price = checkLastPrice(betId, option);

        if (option) {
            return ResponseEntity.ok(BetPrice.builder().betId(betId).yesPrice(price).build());
        } else {
            return ResponseEntity.ok(BetPrice.builder().betId(betId).noPrice(price).build());
        }

    }

    private double checkLastPrice(int betId, boolean option) {
        double price;

        Optional<Transaction> transactions = transactionRepository.findFirstByBetIdAndOptionOrderByTransactionDateDesc(betId, option);

        if (transactions.isPresent()) {
            price = transactions.get().getPrice();
        } else {
            Optional<Contract> optionalContract = contractRepository.findByBetIdAndContractOption(betId, option);
            if (!optionalContract.isPresent()) {
                price = 0;
            } else {
                Contract contract = optionalContract.get();
                Optional<Offer> salesOffer = contract.getOffers().stream().findFirst();
                price = salesOffer.map(Offer::getPrice).orElse(0.0);
            }
        }
        return price;
    }

    private String returnCountOfSharesPossibleToBuy(int shares) {
        return "Znaleziono " + shares + " spełniających podane kryteria";
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    @Retryable(value = MongoCommandException.class,
            maxAttempts = 10, backoff = @Backoff(delay = 100))
    public ResponseEntity<?> buyContract(String username, int betId, int marketId, boolean option, int shares, double maxValue) {
        //Search contracts with matched bets and option with offers
        List<Contract> contracts = contractRepository.findOffersToBuy(betId, option, username);
        if (contracts.isEmpty()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_OFFERS_INFO);

        Player purchaser = playerRepository.findByUsername(username);
        int sharesLast24h = checkBuyingLimit(transactionRepository, username, betId, option, shares);
        if(sharesLast24h + shares > SettingsParams.LIMIT_PER_DAY) return ResponseEntity.badRequest().body("Przekroczyłeś dzienny limit zakupów akcji dla tej opcji zakładu. Możesz kupić "+ (SettingsParams.LIMIT_PER_DAY - sharesLast24h) +" akcji");

        if (purchaser.getBudget() < shares * maxValue) return ResponseEntity.badRequest().body(MarketConstants.LOW_BUDGET_INFO);
        List<Offer> salesOffers = new ArrayList<>();
        int sharesToBuy = 0;
        for (Contract contract : contracts) {
            //Collect all offers where value is less than user maxValue
            salesOffers.addAll(contract.getOffers().stream().filter(offer -> offer.getPrice() <= maxValue).collect(Collectors.toList()));
            sharesToBuy += salesOffers.stream().mapToInt(Offer::getShares).sum();
        }

        Collections.sort(salesOffers);

        if (sharesToBuy < shares) return ResponseEntity.badRequest().body(returnCountOfSharesPossibleToBuy(sharesToBuy));

        if (salesOffers.isEmpty()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_OFFERS_MATCHED_WITH_PREFERENCES_INFO);
        sharesToBuy = shares;
        List<Transaction> transactions = new ArrayList<>();
        for (Offer offer : salesOffers) {
            if (sharesToBuy > 0) {
                Optional<Contract> optContract = contractRepository.findById(offer.getContractId());
                if (!optContract.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.BUYING_ERROR_INFO);
                Contract contract = optContract.get();
                Transaction transaction = makeTransaction(purchaser,offer,contract,sharesToBuy,option);
                transactions.add(transaction);
                sharesToBuy -= transaction.getShares();
            }
        }
        int buyShares = 0;
        double currentPrice = transactions.get(0).getPrice();
        for (Transaction transaction : transactions) {
            if (transaction.getPrice() > currentPrice) {
                currentPrice = transaction.getPrice();
                upsertContractWithSamePrice(contractRepository, predictionMarketRepository, counterService, username, marketId, betId, option, buyShares);
                buyShares = 0;
            }
            buyShares += transaction.getShares();
        }
        transactionRepository.saveAll(transactions);
        Contract contract = upsertContractWithSamePrice(contractRepository, predictionMarketRepository, counterService, username, marketId, betId, option, buyShares);
        double paySum = transactions.stream().mapToDouble(Transaction::getPrice).sum();
        purchaser.setBudget(purchaser.getBudget() - (float) paySum);
        playerRepository.update(purchaser);

        return ResponseEntity.ok(BuyContractResponse.builder().purchaser(purchaser).boughtContract(contract).build());
    }

    private Transaction makeTransaction(Player purchaser, Offer offer, Contract contract, int sharesToBuy, boolean option) {
        Transaction transaction;
        if (sharesToBuy >= offer.getShares()) {
            //Update contract
            contract.deleteOffer(offer.getId());
            salesOfferRepository.delete(offer);
            if (contract.getOffers() == null && contract.getShares() == 0) {
                contractRepository.deleteById(contract.getId());
            } else {
                contractRepository.update(contract);
            }
            //Update seller budget
            if (contract.getPlayerId() != null) updateSellerBudget(contract.getPlayerId(),offer);
            transaction = Transaction.builder().id(counterService.getNextId("transactions")).shares(offer.getShares()).price(offer.getPrice()).marketInfo(contract.getMarketInfo()).bet(contract.getBet()).option(option).purchaser(purchaser.getUsername()).dealer(contract.getPlayerId()).build();
        } else {
            //Case where only part of offer is sold
            offer.setShares(offer.getShares() - sharesToBuy);
            salesOfferRepository.update(offer);
            contract.updateOffer(offer);
            contractRepository.update(contract);
            if (contract.getPlayerId() != null) updateSellerBudget(contract.getPlayerId(),offer);
            transaction = Transaction.builder().id(counterService.getNextId("transactions")).shares(sharesToBuy).price(offer.getPrice()).marketInfo(contract.getMarketInfo()).bet(contract.getBet()).option(option).purchaser(purchaser.getUsername()).dealer(contract.getPlayerId()).build();
        }
        return transaction;
    }

    private void updateSellerBudget(String seller, Offer offer) {
        Player player = playerRepository.findByUsername(seller);
        double money = player.getBudget() + offer.getPrice() * offer.getShares();
        player.setBudget((float) money);
        playerRepository.update(player);
    }

    public ResponseEntity<?> makeMarketPublic(int marketId) {
        Optional<PredictionMarket> marketOpt = predictionMarketRepository.findByMarketId(marketId);

        if (!marketOpt.isPresent()) {
            return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_MARKET_INFO);
        }

        PredictionMarket market = marketOpt.get();

        if (market.getBets() == null) return ResponseEntity.badRequest().body(MarketConstants.FIRST_ADD_BETS_INFO);

        if (market.isPublished()) return ResponseEntity.badRequest().body(MarketConstants.MARKET_IS_ALREADY_PUBLISHED_INFO);

        market.setPublished(true);
        predictionMarketRepository.update(market);

        return ResponseEntity.ok(market);
    }

    @Transactional
    public ResponseEntity<?> solveMarket(int marketId, int correctBetId, boolean correctOption) {

        Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);

        if (!optMarket.isPresent()) return ResponseEntity.badRequest().body(MarketConstants.NOT_FOUND_MARKET_INFO);
        PredictionMarket market = optMarket.get();
        if (market.getCorrectBetId() > 0) return ResponseEntity.badRequest().body(MarketConstants.MARKET_IS_ALREADY_SOLVED_INFO);
        List<Contract> contractsYes = contractRepository.findAllByBetIdAndPlayerIdIsNotNull(correctBetId);
        if (contractsYes.size() > 0) updateContractsAndPlayerBudget(contractsYes, correctOption);

        if(market.getBets().size() > 1){
            List<Bet> bets = market.getBets().stream().filter(bet -> bet.getId() != correctBetId).collect(Collectors.toList());
            for (Bet bet : bets) {
                List<Contract> contracts = contractRepository.findAllByBetIdAndPlayerIdIsNotNull(bet.getId());
                if (contracts.size() > 0) updateContractsAndPlayerBudget(contracts, !correctOption);

            }
        }
        deleteStartContracts(marketId);

        market.setCorrectBetId(correctBetId);
        market.setCorrectBetOption(true);
        market.setEndDate(new SimpleDateFormat(SettingsParams.DATE_FORMAT, SettingsParams.LOCALE_PL).format(new Date()));
        predictionMarketRepository.update(market);

        return ResponseEntity.ok(market);

    }

    private void deleteStartContracts(int marketId) {
        List<Contract> marketContracts = contractRepository.findAllByMarketIdAndPlayerIdIsNull(marketId);
        for (Contract contract : marketContracts) {
            if (contract.getOffers() != null) {
                Offer offer = contract.getOffers().iterator().next();
                salesOfferRepository.delete(offer);
            }
            contractRepository.delete(contract);
        }
    }


    private void updateContractsAndPlayerBudget(List<Contract> contracts, boolean betOption) {
        for (Contract contract : contracts) {
            int countOfShares = contract.getShares();
            if (contract.getOffers() != null) {
            countOfShares += contract.getOffers().stream().mapToInt(Offer::getShares).sum();
            salesOfferRepository.deleteAllByContractId(contract.getId());
            }
            if (contract.isContractOption() == betOption) {
                contract.setContractStatus(ContractStatus.WON);
                updateWinnerBudget(contract.getPlayerId(),countOfShares);
            } else {
                contract.setContractStatus(ContractStatus.LOST);
            }
            contract.setOffers(null);
            contract.setShares(countOfShares);
            contractRepository.update(contract);
        }
    }


    private void updateWinnerBudget(String username, int winShares) {
        Player player = playerRepository.findByUsername(username);
        player.setBudget(player.getBudget() + winShares);
        playerRepository.update(player);
    }

}
