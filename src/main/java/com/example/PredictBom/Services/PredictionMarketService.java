package com.example.PredictBom.Services;

import com.example.PredictBom.Models.BetRequest;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BetPrice;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Models.MarketWithBetsPricesResponse;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Repositories.*;
import com.mongodb.MongoCommandException;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
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
public class PredictionMarketService {

    public static final int MARKET_NOT_FOUND = 1;
    public static final int BET_ADDED = 2;
    public static final int CHANGED_COVER = 3;
    public static final int BET_NOT_FOUND = 4;
    public static final int BET_DELETED = 5;
    public static final String NOT_FOUND_BETS_INFO = "Aby opublikować rynek, dodaj do niego zakłady";
    public static final String NOT_FOUND_BET_INFO = "Nie znaleziono takiego zakładu";
    public static final String MARKET_CREATED_INFO = "Utworzono nowy rynek prognostyczny";
    public static final String MARKET_SOLVED_INFO = "Rozwiązano rynek";
    public static final String NOT_ENOUGH_MONEY_INFO = "Masz za mało pieniędzy";
    public static final String MARKET_EXISTING_INFO = "Rynek o tym tytule już istnieje";
    public static final String NOT_FOUND_CATEGORY_INFO = "Nie znaleziono podanej kategorii";
    public static final String UNLOGGED_USER_CREATING_MARKET_INFO = "Musisz się zalogować aby tworzyć rynki";
    public static final String NOT_FOUND_MARKET_INFO = "Nie znaleziono rynku o podanym id";
    public static final String BET_ADDED_INFO = "Dodano zakład";
    public static final String BET_DELETED_INFO = "Usunięto zakład";
    public static final String COVER_CHANGED_INFO = "Zmienono okładkę rynku";
    public static final String CANNOT_CHANGED_COVER_INFO = "Nie możesz zmienić okładki w opublikowanym rynku";
    public static final String MARKET_DELETED_INFO = "Usunięto rynek";
    public static final String FIRST_DELETE_BET_INFO = "Aby usunąć rynek, najpierw usuń zakłady";
    public static final String MARKET_IS_ALREADY_PUBLISHED_INFO = "Rynek jest już opublikowany";
    public static final String PUBLISHED_MARKET_INFO = "Opublikowano rynek";
    public static final String NOT_FOUND_OFFERS_INFO = "Nie znaleziono żadnych ofert dla wybranego zakładu";
    public static final String LOW_BUDGET_INFO = "Iloczyn maksymalnej ceny 1 akcji i ich liczby nie może być większy od twojego budżetu";
    public static final String NOT_FOUND_OFFERS_MATCHED_WITH_PREFERENCES_INFO = "Nie znaleziono ofert pasujących do twoich preferencji";
    public static final String MARKET_UPDATED_INFO = "Zaltualizowano dane rynku";
    public static final String BOUGHT_SHARES_INFO = "Zakupiono akcje";
    public static final String MARKET_IS_ALREADY_SOLVED_INFO = "Rynek jest już zakończony";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    UserService userService;

    @Autowired
    BetRepository betRepository;

    @Autowired
    CounterService counterService;

    @Autowired
    SalesOfferRepository salesOfferRepository;

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    PredictionMarketRepository predictionMarketRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    PlayerRepository playerRepository;

//    String username, String topic, String category, String predictedDateOfEnd,

    public PredictionMarketResponse createPredictionMarket(String username, String topic, String category, String endDate,String description) {
        Optional<User> userOptional = userService.getUser(username);
        Optional<PredictionMarket> marketOptional = predictionMarketRepository.findByTopic(topic);

        if(!userOptional.isPresent()) return new PredictionMarketResponse(UNLOGGED_USER_CREATING_MARKET_INFO,null);
        if(marketOptional.isPresent()) return new PredictionMarketResponse(MARKET_EXISTING_INFO,null);
        MarketCategory marketCategory;
        switch(category){
            case "SPORT":
                marketCategory = MarketCategory.SPORT;
                break;
            case "ECONOMY":
                marketCategory = MarketCategory.ECONOMY;
                break;
            case "CELEBRITIES":
                marketCategory = MarketCategory.CELEBRITIES;
                break;
            case "POLICY":
                marketCategory = MarketCategory.POLICY;
                break;
            case "OTHER":
                marketCategory = MarketCategory.OTHER;
                break;
            default:
                return new PredictionMarketResponse(NOT_FOUND_CATEGORY_INFO,null);
        }

        if(endDate.length() == 0) endDate = "3000-01-01";

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


        return new PredictionMarketResponse(MARKET_CREATED_INFO,predictionMarket);
    }

        @Transactional
        public MarketWithBetsPricesResponse addBet(BetRequest betRequest)    {

            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(betRequest.getMarketId());

            if(!optionalPredictionMarket.isPresent()) return MarketWithBetsPricesResponse.builder().info(NOT_FOUND_MARKET_INFO).build();

            PredictionMarket predictionMarket = optionalPredictionMarket.get();

            if(predictionMarket.getBets() != null && predictionMarket.getBets().stream().anyMatch(bet -> bet.getTitle().equals(betRequest.getTitle()))) return MarketWithBetsPricesResponse.builder().info("Zakład o tej nazwie już istnieje dla tego rynku").build();

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
            List<Contract> contractsToSave = new ArrayList<>(Arrays.asList(contractFalse,contractTrue));
            contractRepository.saveAll(contractsToSave);
            List<Offer> offersToSave = new ArrayList<>(Arrays.asList(offerFalse,offerTrue));
            salesOfferRepository.saveAll(offersToSave);


            BetPrice betPrice = BetPrice.builder().betId(newBet.getId()).yesPrice(betRequest.getYesPrice()).noPrice(betRequest.getNoPrice()).build();
            return MarketWithBetsPricesResponse.builder().info(BET_ADDED_INFO).predictionMarket(predictionMarket).betPrice(betPrice).build();

//            optionalPredictionMarket.get().;
        }

        @Transactional
        public PredictionMarketResponse deleteBet(int betId) {

            Optional<Bet> optionalBet = betRepository.findById(betId);
            if(!optionalBet.isPresent()) return new PredictionMarketResponse(NOT_FOUND_BET_INFO,null);

            Bet bet = optionalBet.get();

            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(bet.getMarketId());
            if(!optionalPredictionMarket.isPresent()) return new PredictionMarketResponse(NOT_FOUND_MARKET_INFO,null);

            PredictionMarket marketToDeleteBet = optionalPredictionMarket.get();

            marketToDeleteBet.deleteBet(betId);
            if(marketToDeleteBet.getBets().size() == 0) marketToDeleteBet.setBets(null);
            predictionMarketRepository.update(marketToDeleteBet);
            betRepository.deleteBetById(betId);
            List<Contract> contractList = contractRepository.deleteByBetId(betId);
            for(Contract contract : contractList) {
                salesOfferRepository.deleteByContractId(contract.getId());
            }


//           contractRepository.deleteByBetId(betId);

            return new PredictionMarketResponse(BET_DELETED_INFO,marketToDeleteBet);
        }

        public List<PredictionMarket> getAllPredictionMarkets(){
            return predictionMarketRepository.findAll();
        }

        public List<PredictionMarket> getPredictionMarketsWhereBetsIsNullByAuthor(String author) {

        Optional<User> userOptional = userService.getUser(author);
        if(!userOptional.isPresent()) return null;

        return predictionMarketRepository.findByBetsIsNullAndAuthor(author,Sort.by(Sort.Direction.ASC,"predictedDateEnd"));
    }

        public PredictionMarket getMarketById(int id) {
            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);

            return optionalPredictionMarket.orElse(null);
        }

        @Transactional
        public PredictionMarketResponse setMarketCover(int id, MultipartFile marketCover) throws IOException {

            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);

            if(!optionalPredictionMarket.isPresent()) return new PredictionMarketResponse(NOT_FOUND_MARKET_INFO,null);
            PredictionMarket predictionMarket = optionalPredictionMarket.get();
            if(predictionMarket.isPublished()) return new PredictionMarketResponse(CANNOT_CHANGED_COVER_INFO,null);
            predictionMarket.setMarketCover(new Binary(BsonBinarySubType.BINARY, marketCover.getBytes()));
            if(predictionMarket.getBets() != null) {
                List<Contract> contracts = contractRepository.findAllByMarketId(id);
                for (Contract contract : contracts) {
                    MarketInfo marketInfo = contract.getMarketInfo();
                    marketInfo.setMarketCover(new Binary(BsonBinarySubType.BINARY, marketCover.getBytes()));
                    contract.setMarketInfo(marketInfo);
                    contractRepository.update(contract);
                }
            }
            predictionMarketRepository.update(predictionMarket);


            return new PredictionMarketResponse(COVER_CHANGED_INFO,predictionMarket);
        }

        public List<PredictionMarket> getFilteredMarketsWaitingForBets(String username, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {

            Optional<User> userOptional = userService.getUser(username);
            if(!userOptional.isPresent()) return null;

            List<PredictionMarket> predictionMarketsList = new ArrayList<>(predictionMarketRepository.findByBetsIsNullAndAuthor(username, Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute)));
            return getPredictionMarkets(marketTitle, marketCategory, predictionMarketsList);
        }

    public List<PredictionMarket> getPublicModMarkets(String username, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {

        Optional<User> userOptional = userService.getUser(username);
        if(!userOptional.isPresent()) return null;

        List<PredictionMarket> predictionMarketsList = new ArrayList<>(predictionMarketRepository.findByPublishedTrueAndAuthor(username, Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute)));
        return getPredictionMarkets(marketTitle, marketCategory, predictionMarketsList);
    }

        public List<PredictionMarket> getFilteredPrivateMarkets(String username, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection){
            Optional<User> userOptional = userService.getUser(username);
            if(!userOptional.isPresent()) return null;

            List<PredictionMarket> predictionMarketsList = new ArrayList<>(predictionMarketRepository.findByPublishedFalse(username,Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute)));

            return getPredictionMarkets(marketTitle, marketCategory, predictionMarketsList);
        }

        public PredictionMarketResponse editMarket(int marketId, String topic,String category, String endDate, String description) {
            Optional<PredictionMarket> marketOptional = predictionMarketRepository.findByMarketId(marketId);
            if(!marketOptional.isPresent()) return new PredictionMarketResponse(NOT_FOUND_MARKET_INFO,null);

            PredictionMarket marketToEdit = marketOptional.get();

            marketToEdit.setTopic(topic);
            marketToEdit.setEndDate(endDate);
            marketToEdit.setDescription(description);
            MarketCategory marketCategory;
            switch(category){
                case "SPORT":
                    marketCategory = MarketCategory.SPORT;
                    break;
                case "ECONOMY":
                    marketCategory = MarketCategory.ECONOMY;
                    break;
                case "CELEBRITIES":
                    marketCategory = MarketCategory.CELEBRITIES;
                    break;
                case "POLICY":
                    marketCategory = MarketCategory.POLICY;
                    break;
                case "OTHER":
                    marketCategory = MarketCategory.OTHER;
                    break;
                default:
                    return new PredictionMarketResponse(NOT_FOUND_CATEGORY_INFO,null);
            }

            marketToEdit.setCategory(marketCategory);
            if(marketToEdit.getBets() != null) {
                List<Contract> contracts = contractRepository.findAllByMarketId(marketId);
                for (Contract contract : contracts) {
                    MarketInfo marketInfo = contract.getMarketInfo();
                    marketInfo.setTopic(topic);
                    marketInfo.setMarketCategory(marketCategory);
                    contractRepository.update(contract);
                }
            }
            predictionMarketRepository.update(marketToEdit);
            return new PredictionMarketResponse(MARKET_UPDATED_INFO,marketToEdit);
        }

        // TODO: 10.10.2020
        public PredictionMarketResponse deleteMarket(int marketId) {

            Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);
            if(!optMarket.isPresent())  return PredictionMarketResponse.builder().info(NOT_FOUND_MARKET_INFO).build();

            PredictionMarket market = optMarket.get();
            if(market.getBets() != null) return PredictionMarketResponse.builder().info(FIRST_DELETE_BET_INFO).build();

            predictionMarketRepository.deleteByMarketId(marketId);

            return PredictionMarketResponse.builder().info(MARKET_DELETED_INFO).predictionMarket(market).build();
        }

    private List<PredictionMarket> getPredictionMarkets(String marketTitle, String[] marketCategory, List<PredictionMarket> predictionMarketsList) {
        List<PredictionMarket> marketsFilteredByTitle = predictionMarketsList.stream().filter(item -> item.getTopic().toLowerCase().contains(marketTitle.toLowerCase())).collect(Collectors.toList());
        if(marketCategory.length == 0) return marketsFilteredByTitle;
        List<PredictionMarket> filteredMarkets = new ArrayList<>();
        for(String market : marketCategory) {
            filteredMarkets.addAll(marketsFilteredByTitle.stream().filter(item -> item.getCategory().toString().toLowerCase().contains(market.toLowerCase())).collect(Collectors.toList()));
        }
        return filteredMarkets;
    }

    public BetPrice getPrice(int betId) {

        double yesPrice,noPrice;

        Optional<Transaction> transactionYes =  transactionRepository.findFirstByBetIdAndOptionOrderByTransactionDateDesc(betId, true);
        Optional<Transaction> transactionNo = transactionRepository.findFirstByBetIdAndOptionOrderByTransactionDateDesc(betId,false);

        if(transactionYes.isPresent()){
            yesPrice = transactionYes.get().getPrice();
        }else{
            Optional<Contract> optionalContract  = contractRepository.findByBetIdAndContractOption(betId, true);
            if(!optionalContract.isPresent()) {
                yesPrice = 0;
            }else{
                Contract contract = optionalContract.get();
                Optional<Offer> salesOffer = contract.getOffers().stream().findFirst();
                yesPrice = salesOffer.map(Offer::getPrice).orElse(0.0);
            }
        }

        if(transactionNo.isPresent()){
            noPrice = transactionNo.get().getPrice();
        }else{
            Optional<Contract> optionalContract  = contractRepository.findByBetIdAndContractOption(betId, false);
            if(!optionalContract.isPresent()) {
                noPrice = 0;
            }else{
                Contract contract = optionalContract.get();
                Optional<Offer> salesOffer = contract.getOffers().stream().findFirst();
                noPrice = salesOffer.map(Offer::getPrice).orElse(0.0);
            }

        }

        return BetPrice.builder().betId(betId).yesPrice(yesPrice).noPrice(noPrice).build();

    }

    public BetPrice getLastPrice(int betId, boolean option) {
        double price= 0;

        Optional<Transaction> transactionYes =  transactionRepository.findFirstByBetIdAndOptionOrderByTransactionDateDesc(betId, option);

        if(transactionYes.isPresent()){
            price = transactionYes.get().getPrice();
        }else{
            Optional<Contract> optionalContract  = contractRepository.findByBetIdAndContractOption(betId, option);
            if(!optionalContract.isPresent()) {
                price = 0;
            }else{
                Contract contract = optionalContract.get();
                Optional<Offer> salesOffer = contract.getOffers().stream().findFirst();
                price = salesOffer.map(Offer::getPrice).orElse(0.0);
            }
        }

        if(option) {
            return BetPrice.builder().betId(betId).yesPrice(price).build();
        } else{
            return BetPrice.builder().betId(betId).noPrice(price).build();
        }

    }

    public String returnLimitInfo(int sharesToBuy) {
        return "Przekroczyłeś dzienny limit zakupów akcji dla tej opcji zakładu. Możesz kupić "+ (sharesToBuy) +" akcji";
    }

    public String returnCountOfSharesPossibleToBuy(int shares) {
        return "Znaleziono "+shares+ " spełniających podane kryteria";
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    @Retryable( value = MongoCommandException.class,
            maxAttempts = 10, backoff = @Backoff(delay = 100))
    public BuyContractResponse buyContract(String username,int betId, int marketId, boolean option, int shares, double maxValue) {
        //Search contracts with matched bets and option with offers
        List<Contract> contracts = contractRepository.findOffersToBuy(betId,option,username);

        Player purchaser = playerRepository.findByUsername(username);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        String date24hAgo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
        List<Transaction> userTransactions = transactionRepository.findAllByPurchaserAndBetIdAndOptionInLast24hours(username,betId,option,date24hAgo);
        int sumShares = userTransactions.stream().mapToInt(Transaction::getShares).sum();
        if(sumShares + shares > 1000) return BuyContractResponse.builder().info(returnLimitInfo(1000-sumShares)).build();

        //Not found any offers
        if(contracts.isEmpty()) return BuyContractResponse.builder().info(NOT_FOUND_OFFERS_INFO).build();
        if(purchaser.getBudget() < shares*maxValue) return BuyContractResponse.builder().info(LOW_BUDGET_INFO).build();
        List<Offer> salesOffers = new ArrayList<>();
        int sharesToBuy = 0;
        int counter = 0;
        for(Contract contract : contracts){
            //Collect all offers where value is less than user maxValue
            salesOffers.addAll(contract.getOffers().stream().filter(offer -> offer.getPrice() <= maxValue).collect(Collectors.toList()));
            sharesToBuy += salesOffers.stream().mapToInt(Offer::getShares).sum();
            counter++;
        }

        Collections.sort(salesOffers);


        if(sharesToBuy < shares) return BuyContractResponse.builder().info(returnCountOfSharesPossibleToBuy(sharesToBuy)).build();
        if(salesOffers.isEmpty()) return BuyContractResponse.builder().info(NOT_FOUND_OFFERS_MATCHED_WITH_PREFERENCES_INFO).build();
        double paySum = 0;
        sharesToBuy = shares;
        List<Transaction> transactions = new ArrayList<>();
        for(Offer offer : salesOffers) {
            if(sharesToBuy > 0) {
                //Case where whole offer is sold
                if(sharesToBuy >= offer.getShares()){
                    //Update contract
                    sharesToBuy -= offer.getShares();
                    Optional<Contract> optContract = contractRepository.findById(offer.getContractId());
                    if(!optContract.isPresent()) return null;
                    Contract contract = optContract.get();
                    contract.deleteOffer(offer.getId());
                    salesOfferRepository.delete(offer);
                    if(contract.getOffers() == null && contract.getShares() == 0) {
                        contractRepository.deleteById(contract.getId());
                    }else{
                        contractRepository.update(contract);
                    }
                    //Update seller budget
                    if(contract.getPlayerId() != null){
                        Player player = playerRepository.findByUsername(contract.getPlayerId());
                        double money = player.getBudget()+offer.getPrice()*offer.getShares();
                        player.setBudget((float) money);
                        playerRepository.update(player);
                    }
                    //Update purchaser money

                    Transaction transaction = Transaction.builder().id(counterService.getNextId("transactions")).shares(offer.getShares()).price(offer.getPrice()).marketInfo(contract.getMarketInfo()).bet(contract.getBet()).option(option).purchaser(purchaser.getUsername()).dealer(contract.getPlayerId()).build();
                    transactionRepository.save(transaction);
                    transactions.add(transaction);
                    paySum += offer.getPrice() * offer.getShares();

                //Case where only part of offer is sold
                }else{
                    paySum += sharesToBuy * offer.getPrice();
                    offer.setShares(offer.getShares() - sharesToBuy);
                    Optional<Contract> optContract = contractRepository.findById(offer.getContractId());
                    if(!optContract.isPresent()) return null;
                    Contract contract = optContract.get();
                    salesOfferRepository.update(offer);
                    contract.deleteOffer(offer.getId());
                    contract.addOffer(offer);
                    contractRepository.update(contract);
                    if(contract.getPlayerId() != null){
                        Player player = playerRepository.findByUsername(contract.getPlayerId());
                        double money = player.getBudget()+offer.getPrice()*offer.getShares();
                        player.setBudget((float) money);
                        playerRepository.update(player);
                    }
                    //Update purchaser money
                    Transaction transaction = Transaction.builder().id(counterService.getNextId("transactions")).shares(sharesToBuy).price(offer.getPrice()).marketInfo(contract.getMarketInfo()).bet(contract.getBet()).option(option).purchaser(purchaser.getUsername()).dealer(contract.getPlayerId()).build();
                    transactionRepository.save(transaction);
                    transactions.add(transaction);
                    sharesToBuy = 0;
                }


            }
        }
        int buyShares = 0;
        double currentPrice = transactions.get(0).getPrice();
        for(Transaction transaction : transactions) {
            if(transaction.getPrice() > currentPrice){
                currentPrice = transaction.getPrice();
                findContractWithSamePrice(username,marketId, betId, option, buyShares);
                buyShares = 0;
            }
            buyShares += transaction.getShares();
        }

        Contract contract = findContractWithSamePrice(username,marketId, betId, option, buyShares);
        purchaser.setBudget(purchaser.getBudget() - (float)paySum);
        playerRepository.update(purchaser);

        return BuyContractResponse.builder().info(BOUGHT_SHARES_INFO).purchaser(purchaser).boughtContract(contract).build();
    }

    private Contract findContractWithSamePrice(String username,int marketId, int betId, boolean option, int buyShares) {
        Optional<Contract> optionalContract = contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId,option);
        Contract contract;
        if(optionalContract.isPresent()){
            contract = optionalContract.get();
            contract.setShares(contract.getShares() + buyShares);
            contract.setModifiedDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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

    public PredictionMarketResponse makeMarketPublic(int marketId) {
        Optional<PredictionMarket> marketOpt = predictionMarketRepository.findByMarketId(marketId);

        if(!marketOpt.isPresent()) {
            return new PredictionMarketResponse(NOT_FOUND_MARKET_INFO,null);
        }

        PredictionMarket market = marketOpt.get();

        if(market.getBets() == null) return new PredictionMarketResponse(NOT_FOUND_BETS_INFO,null);

        if(market.isPublished()) return new PredictionMarketResponse(MARKET_IS_ALREADY_PUBLISHED_INFO,null);

        market.setPublished(true);
        predictionMarketRepository.update(market);

        return new PredictionMarketResponse(PUBLISHED_MARKET_INFO,market);
    }

    public List<PredictionMarket> getPublicMarkets(String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {

        List<PredictionMarket> predictionMarketsList = new ArrayList<>(predictionMarketRepository.findPublishedNotSolvedMarkets(Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute)));
        return getPredictionMarkets(marketTitle, marketCategory, predictionMarketsList);
    }

    public List<PredictionMarket> getSolvedMarkets(String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {

        List<PredictionMarket> predictionMarketsList = new ArrayList<>(predictionMarketRepository.findSolvedMarkets(Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute)));
        return getPredictionMarkets(marketTitle, marketCategory, predictionMarketsList);
    }

    @Transactional
    public PredictionMarketResponse solveMultiBetMarket(int marketId,int correctBetId) {

        Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);

        if(!optMarket.isPresent()) return new PredictionMarketResponse(NOT_FOUND_MARKET_INFO,null);
        PredictionMarket market = optMarket.get();
        if(market.getCorrectBetId() > 0) return new PredictionMarketResponse(MARKET_IS_ALREADY_SOLVED_INFO,null);
        List<Contract> contractsYes = contractRepository.findAllByBetIdAndPlayerIdIsNotNull(correctBetId);
        if(contractsYes.size() > 0) updatePlayersBudget(contractsYes,true);


        List<Bet> bets = market.getBets().stream().filter(bet -> bet.getId() != correctBetId).collect(Collectors.toList());
        for(Bet bet : bets) {
            List<Contract> contracts = contractRepository.findAllByBetIdAndPlayerIdIsNotNull(bet.getId());

            if(contracts.size() > 0)updatePlayersBudget(contracts,false);

        }

        market.setCorrectBetId(correctBetId);
        market.setCorrectBetOption(true);
        market.setEndDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        predictionMarketRepository.update(market);
        List<Contract> marketContracts = contractRepository.findAllByMarketIdAndPlayerIdIsNull(marketId);
        for (Contract contract : marketContracts) {
            if(contract.getOffers() != null) {
                Offer offer = contract.getOffers().iterator().next();
                salesOfferRepository.delete(offer);
            }
            contractRepository.delete(contract);
        }
        return new PredictionMarketResponse(MARKET_SOLVED_INFO,market);

}

    @Transactional
    public PredictionMarketResponse solveSingleBetMarket(int marketId,int betId,boolean correctOption) {

        Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);

        if(!optMarket.isPresent()) return new PredictionMarketResponse(NOT_FOUND_MARKET_INFO,null);
        PredictionMarket market = optMarket.get();
        if(market.getCorrectBetId() > 0) return new PredictionMarketResponse(MARKET_IS_ALREADY_SOLVED_INFO,null);
        List<Contract> contracts = contractRepository.findAllByBetIdAndPlayerIdIsNotNull(betId);
        if(contracts.size() > 0) updatePlayersBudget(contracts,correctOption);


        List<Contract> marketContracts = contractRepository.findAllByMarketIdAndPlayerIdIsNull(marketId);
        for (Contract contract : marketContracts) {
            if(contract.getOffers() != null) {
                Offer offer = contract.getOffers().iterator().next();
                salesOfferRepository.delete(offer);
            }
            contractRepository.delete(contract);
        }
//        contractRepository.deleteAllByPlayerIdIsNull();
        market.setCorrectBetOption(correctOption);
        market.setCorrectBetId(betId);
        market.setEndDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        predictionMarketRepository.update(market);

        return new PredictionMarketResponse(MARKET_SOLVED_INFO,market);

    }


    private void updatePlayersBudget(List<Contract> contracts,boolean betOption) {
        for(Contract contract : contracts) {
            int countOfShares = contract.getShares();
            if(contract.getOffers() != null)  {
                for (Offer offer : contract.getOffers()){
                    countOfShares += offer.getShares();
                    salesOfferRepository.deleteById(offer.getId());
                }

            }

            setCorrectBetInfo(betOption, contract, countOfShares);
        }
    }

//    private void updatePlayersBudget(List<Contract> contracts,boolean correctOption) {
//        for(Contract contract : contracts) {
//            int countOfShares = contract.getCountOfContracts();
//            if(contract.getOffers() != null)    for (SalesOffer offer : contract.getOffers()) countOfShares += offer.getCountOfContracts();
//            setCorrectBetInfo(correctOption, contract, countOfShares);
//        }
//    }

    private void setCorrectBetInfo(boolean correctOption, Contract contract, int countOfShares) {
        if (contract.isContractOption() == correctOption) {
            contract.setContractStatus(ContractStatus.WON);
            Player player = playerRepository.findByUsername(contract.getPlayerId());
            player.setBudget(player.getBudget() + countOfShares);
            playerRepository.update(player);
        } else {
            contract.setContractStatus(ContractStatus.LOST);
        }
        contract.setOffers(null);
        contract.setShares(countOfShares);
        contractRepository.update(contract);
    }
}
