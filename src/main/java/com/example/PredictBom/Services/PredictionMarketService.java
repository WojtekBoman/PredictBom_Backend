package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BetPrice;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Models.MarketWithBetsPricesResponse;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Repositories.*;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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

    public PredictionMarketResponse createPredictionMarket(String username, String topic, String category, String predictedDateEnd,String description) throws IOException {

        Optional<User> userOptional = userService.getUser(username);
        Optional<PredictionMarket> marketOptional = predictionMarketRepository.findByTopic(topic);


        if(!userOptional.isPresent()) return new PredictionMarketResponse("Zaloguj się aby dodać rynek",null);
        if(marketOptional.isPresent()) return new PredictionMarketResponse("Rynek o tym tytule już istnieje",null);

        MarketCategory marketCategory;

        switch(category){
            case "sport":
                marketCategory = MarketCategory.SPORT;
                break;
            case "gosp":
                marketCategory = MarketCategory.GOSPODARKA;
                break;
            case "cel":
                marketCategory = MarketCategory.CELEBRYCI;
                break;
            case "pol":
                marketCategory = MarketCategory.POLITYKA;
                break;
            case "inne":
                marketCategory = MarketCategory.INNE;
                break;
            default:
                return new PredictionMarketResponse("Wybrano błędną kategorie",null);
        }

        if(predictedDateEnd.length() == 0) predictedDateEnd = "3000-01-01";

        PredictionMarket predictionMarket = PredictionMarket
                .builder()
                .marketId(counterService.getNextId("markets"))
                .topic(topic)
                .category(marketCategory)
                .author(username)
                .predictedEndDate(predictedDateEnd)
                .description(description)
                .build();
        predictionMarketRepository.save(predictionMarket);


        return new PredictionMarketResponse("Utworzono nowy rynek prognostyczny",predictionMarket);
    }

        @Transactional
        public MarketWithBetsPricesResponse addBet(int id, int yesPrice, int noPrice, String chosenOption)    {

            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);

            if(!optionalPredictionMarket.isPresent()) return MarketWithBetsPricesResponse.builder().info("Nie znaleziono rynku").build();
            Bet newBet = Bet
                .builder()
                .id(counterService.getNextId("bets"))
                 .marketId(id)
                .chosenOption(chosenOption)
                .build();
            PredictionMarket predictionMarket = optionalPredictionMarket.get();
            predictionMarket.addBet(newBet);
            predictionMarketRepository.update(predictionMarket);
            betRepository.save(newBet);

            double contractYesPrice =  Math.round((double)yesPrice) / 100.0;
            double contractNoPrice = Math.round((double)noPrice) /100.0;

            PredictionMarket contractMarket = predictionMarket.toBuilder().build();
            contractMarket.setBets(null);
            Contract contractTrue = Contract.builder().id(counterService.getNextId("contracts")).bet(newBet).countOfContracts(0).contractOption(true).predictionMarket(contractMarket).build();
            Contract contractFalse = Contract.builder().id(counterService.getNextId("contracts")).bet(newBet).countOfContracts(0).contractOption(false).predictionMarket(contractMarket).build();
            SalesOffer offerTrue = SalesOffer.builder().id(counterService.getNextId("offers")).contractId(contractTrue.getId()).countOfContracts(10000).valueOfShares(contractYesPrice).build();
            SalesOffer offerFalse = SalesOffer.builder().id(counterService.getNextId("offers")).contractId(contractFalse.getId()).countOfContracts(10000).valueOfShares(contractNoPrice).build();
            contractTrue.addOffer(offerTrue);
            contractFalse.addOffer(offerFalse);
            contractRepository.save(contractTrue);
            contractRepository.save(contractFalse);
            salesOfferRepository.save(offerTrue);
            salesOfferRepository.save(offerFalse);

            BetPrice betPrice = BetPrice.builder().betId(newBet.getId()).yesPrice(yesPrice).noPrice(noPrice).build();
            return MarketWithBetsPricesResponse.builder().info("Dodano zakład").predictionMarket(predictionMarket).betPrice(betPrice).build();

//            optionalPredictionMarket.get().;
        }

        @Transactional
        public PredictionMarketResponse deleteBet(int marketId, int betId) {
            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(marketId);
            if(!optionalPredictionMarket.isPresent()) return new PredictionMarketResponse("Nie znaleziono takiego rynku",null);

            Optional<Bet> optionalBet = betRepository.findById(betId);
            if(!optionalBet.isPresent()) return new PredictionMarketResponse("Nie znaleziono takiego zakładu",null);

            PredictionMarket marketToDeleteBet = optionalPredictionMarket.get();

            marketToDeleteBet.deleteBet(betId);
            if(marketToDeleteBet.getBets().size() == 0) marketToDeleteBet.setBets(null);
            predictionMarketRepository.update(marketToDeleteBet);
            betRepository.deleteBetById(betId);
//            contractRepository.deleteByBetId(betId);
            List<Contract> contractList = contractRepository.deleteByBetId(betId);
            for(Contract contract : contractList) {

                salesOfferRepository.deleteByContractId(contract.getId());
            }


//           contractRepository.deleteByBetId(betId);

            return new PredictionMarketResponse("Usunięto zakład",marketToDeleteBet);
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

            if(!optionalPredictionMarket.isPresent()) return new PredictionMarketResponse("Nie znaleziono rynku prognostycznego",null);

            PredictionMarket predictionMarket = optionalPredictionMarket.get();
            predictionMarket.setMarketCover(new Binary(BsonBinarySubType.BINARY, marketCover.getBytes()));
            predictionMarketRepository.update(predictionMarket);

            List<Contract> contracts = contractRepository.findAllByMarketId(id);
            if(contracts.size() > 0) {
                for(Contract contract : contracts) {
                    contract.setPredictionMarket(predictionMarket);
                    contractRepository.update(contract);
                }
            }

            return new PredictionMarketResponse("Zmieniono okładkę",predictionMarket);
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

        public PredictionMarketResponse editMarket(int marketId, String marketTitle, String predictedDateEnd, String description) {
            Optional<PredictionMarket> marketOptional = predictionMarketRepository.findByMarketId(marketId);
            if(!marketOptional.isPresent()) return new PredictionMarketResponse("Nie znaleziono rynku o podanym identyfikatorze",null);

            PredictionMarket marketToEdit = marketOptional.get();

            if(marketTitle.length() > 0) marketToEdit.setTopic(marketTitle);
            if(predictedDateEnd.length() > 0) marketToEdit.setPredictedEndDate(predictedDateEnd);
            if(description.length() > 0) marketToEdit.setDescription(description);

            predictionMarketRepository.update(marketToEdit);
            return new PredictionMarketResponse("Informacje o rynku został zaktualizowane",marketToEdit);
        }

        // TODO: 10.10.2020
//        public PredictionMarketResponse deleteMarket(int marketId) {
//            return null
//        }

    private List<PredictionMarket> getPredictionMarkets(String marketTitle, String[] marketCategory, List<PredictionMarket> predictionMarketsList) {
        System.out.println("Jestem tutaj");
        List<PredictionMarket> marketsFilteredByTitle = predictionMarketsList.stream().filter(item -> item.getTopic().toLowerCase().contains(marketTitle.toLowerCase())).collect(Collectors.toList());
        System.out.println("Jestem tutaj");
        if(marketCategory.length == 0) return marketsFilteredByTitle;
        List<PredictionMarket> filteredMarkets = new ArrayList<>();
        for(String market : marketCategory) {
            filteredMarkets.addAll(marketsFilteredByTitle.stream().filter(item -> item.getCategory().toString().toLowerCase().contains(market.toLowerCase())).collect(Collectors.toList()));
        }
        return filteredMarkets;
    }

//    private List<BetPrice> getPrices(int marketId){
//        Optional<PredictionMarket> marketOptional = predictionMarketRepository.findByMarketId(marketId);
//        if(marketOptional.isPresent()) return null;
//
//        for(Bet bet :marketOptional.get().getBets()){
//            Contract contract = contractRepository.findByBetId(bet)
//        }
//    }

    public BetPrice getPrice(int betId) {

        double yesPrice,noPrice = 0;

        Optional<Transaction> transactionYes =  transactionRepository.findFirstByBetIdAndAndOptionOrderByTransactionDateDesc(betId, true);
        Optional<Transaction> transactionNo = transactionRepository.findFirstByBetIdAndAndOptionOrderByTransactionDateDesc(betId,false);

        if(transactionYes.isPresent()){
            yesPrice = transactionYes.get().getPrice();
        }else{
            Contract contract = contractRepository.findByBetIdAndContractOption(betId, true);
            yesPrice = contract.getOffers().stream().findFirst().get().getValueOfShares();
        }

        if(transactionNo.isPresent()){
            noPrice = transactionNo.get().getPrice();
        }else{
            Contract contract = contractRepository.findByBetIdAndContractOption(betId, false);
            noPrice = contract.getOffers().stream().findFirst().get().getValueOfShares();
        }

        return BetPrice.builder().betId(betId).yesPrice(yesPrice).noPrice(noPrice).build();

    }

    private double getLastYesPrice(int betId) {
        double yesPrice= 0;

        Optional<Transaction> transactionYes =  transactionRepository.findFirstByBetIdAndAndOptionOrderByTransactionDateDesc(betId, true);

        if(transactionYes.isPresent()){
            yesPrice = transactionYes.get().getPrice();
        }else{
            Contract contract = contractRepository.findByBetIdAndContractOption(betId, true);
            yesPrice = contract.getOffers().stream().findFirst().get().getValueOfShares();
        }

        return yesPrice;
    }

    private double getLastNoPrice(int betId) {
        double noPrice= 0;
        Optional<Transaction> transactionNo = transactionRepository.findFirstByBetIdAndAndOptionOrderByTransactionDateDesc(betId,false);

        if(transactionNo.isPresent()){
            noPrice = transactionNo.get().getPrice();
        }else{
            Contract contract = contractRepository.findByBetIdAndContractOption(betId, false);
            noPrice = contract.getOffers().stream().findFirst().get().getValueOfShares();
        }

        return noPrice;
    }

    @Transactional
    public BuyContractResponse buyContract(String username,int betId, int marketId, boolean option, int countOfShares, int maxValue) {
        //Search contracts with matched bets and option with offers
        System.out.println("Contract option" + betId);
        List<Contract> contracts = contractRepository.findAllByBetIdAndContractOptionAndOffersIsNotNull(betId,option);

        Player purchaser = playerRepository.findByUsername(username);

        //Not found any offers
        if(contracts.isEmpty()) return BuyContractResponse.builder().info("Nie znaleziono żadnych ofert dla wybranego zakładu").build();

        List<SalesOffer> salesOffers = new ArrayList<>();
        double maxValueFloat =  Math.round((double)maxValue) / 100.0;
        System.out.println(maxValueFloat);
        int shares = 0;
        int counter = 0;
        for(Contract contract : contracts){
            //Collect all offers where value is less than user maxValue
            salesOffers.addAll(contract.getOffers().stream().filter(offer -> offer.getValueOfShares() <= maxValueFloat).collect(Collectors.toList()));
            shares += salesOffers.stream().mapToInt(SalesOffer::getCountOfContracts).sum();
            counter++;
        }

        Collections.sort(salesOffers);

        if(shares < countOfShares) return BuyContractResponse.builder().info("Nie znaleziono wystarczającej liczby kontraktów").build();
        if(salesOffers.isEmpty()) return BuyContractResponse.builder().info("Nie znaleziono ofert pasujących do twoich preferencji").build();
        double paySum = 0;
        shares = countOfShares;
        List<Transaction> transactions = new ArrayList<>();
        for(SalesOffer offer : salesOffers) {
            if(shares > 0) {
                //Case where whole offer is sold
                if(countOfShares > offer.getCountOfContracts()){
                    //Update contract
                    paySum = offer.getValueOfShares() * offer.getCountOfContracts();
                    shares -= offer.getCountOfContracts();
                    Contract contract = contractRepository.findById(offer.getContractId());
                    contract.deleteOffer(offer.getId());
                    salesOfferRepository.delete(offer);
                    //Update seller budget
                    if(contract.getPlayerId() != null){
                        Player player = playerRepository.findByUsername(contract.getPlayerId());
                        double money = player.getBudget()+offer.getValueOfShares()*offer.getCountOfContracts();
                        player.setBudget((float) money);
                        playerRepository.update(player);
                    }
                    //Update purchaser money

                    Transaction transaction = Transaction.builder().id(counterService.getNextId("transactions")).countOfShares(offer.getCountOfContracts()).price(offer.getValueOfShares()).betId(betId).option(option).purchaser(purchaser.getUsername()).dealer(contract.getPlayerId()).build();
                    transactionRepository.save(transaction);
                    transactions.add(transaction);
                    paySum += offer.getValueOfShares() * offer.getCountOfContracts();

                //Case where only part of offer is sold
                }else{
                    paySum = shares * offer.getValueOfShares();
                    offer.setCountOfContracts(offer.getCountOfContracts() - shares);
                    Contract contract = contractRepository.findById(offer.getContractId());
                    salesOfferRepository.update(offer);
                    contract.deleteOffer(offer.getId());
                    contract.addOffer(offer);
                    contractRepository.update(contract);
                    if(contract.getPlayerId() != null){
                        Player player = playerRepository.findByUsername(contract.getPlayerId());
                        double money = player.getBudget()+offer.getValueOfShares()*offer.getCountOfContracts();
                        player.setBudget((float) money);
                        playerRepository.update(player);
                    }
                    //Update purchaser money
                    Transaction transaction = Transaction.builder().id(counterService.getNextId("transactions")).countOfShares(shares).price(offer.getValueOfShares()).betId(betId).option(option).purchaser(purchaser.getUsername()).dealer(contract.getPlayerId()).build();
                    transactionRepository.save(transaction);
                    transactions.add(transaction);
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
            buyShares += transaction.getCountOfShares();
        }

        Contract contract = findContractWithSamePrice(username,marketId, betId, option, buyShares);
        purchaser.setBudget(purchaser.getBudget() - (float)paySum);
        playerRepository.update(purchaser);

        return BuyContractResponse.builder().info("Zakupiono nowy kontrakt").purchaser(purchaser).boughtContract(contract).build();
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
            market.setBets(null);
            contract = Contract.builder().id(counterService.getNextId("contracts")).bet(bet.iterator().next()).contractOption(option).countOfContracts(buyShares).predictionMarket(market).playerId(username).build();
            contractRepository.save(contract);
        }
        return contract;
    }

    public PredictionMarketResponse makeMarketPublic(int marketId) {
        Optional<PredictionMarket> marketOpt = predictionMarketRepository.findByMarketId(marketId);

        if(!marketOpt.isPresent()) {
            return new PredictionMarketResponse("Nie znalezieono rynku",null);
        }

        PredictionMarket market = marketOpt.get();

        if(market.isPublished()) return new PredictionMarketResponse("Rynek jest już publiczny",null);

        market.setPublished(true);
        predictionMarketRepository.update(market);
        List<Contract> contracts = contractRepository.findAllByMarketId(marketId);
        for(Contract contract : contracts) {
            contract.setPredictionMarket(market);
            contractRepository.update(contract);
        }

        return new PredictionMarketResponse("Upubliczniono rynek",market);
    }

    public List<PredictionMarket> getPublicMarkets(String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {

        List<PredictionMarket> predictionMarketsList = new ArrayList<>(predictionMarketRepository.findByPublishedTrue(Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute)));
        System.out.println(predictionMarketsList.size());
        return getPredictionMarkets(marketTitle, marketCategory, predictionMarketsList);
    }

    @Transactional
    public PredictionMarketResponse solveMultiBetMarket(int marketId,int correctBetId) {

        Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);

        if(!optMarket.isPresent()) return new PredictionMarketResponse("Nie znaleziono rynku",null);
        PredictionMarket market = optMarket.get();
        List<Contract> contractsYes = contractRepository.findAllByBetIdAndPlayerIdIsNotNull(correctBetId);
        if(contractsYes.size() > 0) updatePlayersBudget(contractsYes,correctBetId,true);


        List<Bet> bets = market.getBets().stream().filter(bet -> bet.getId() != correctBetId).collect(Collectors.toList());
        for(Bet bet : bets) {
            List<Contract> contracts = contractRepository.findAllByBetIdAndPlayerIdIsNotNull(bet.getId());
            if(contracts.size() > 0)updatePlayersBudget(contracts,correctBetId,true);

        }

        market.setCorrectBetId(correctBetId);
        predictionMarketRepository.update(market);

        return new PredictionMarketResponse("Rozwiązano rynek",market);

}

    @Transactional
    public PredictionMarketResponse solveSingleBetMarket(int marketId,int betId,boolean correctOption) {

        Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(marketId);

        if(!optMarket.isPresent()) return new PredictionMarketResponse("Nie znaleziono rynku",null);
        PredictionMarket market = optMarket.get();
        List<Contract> contracts = contractRepository.findAllByBetIdAndContractOptionAndPlayerIdIsNotNull(betId,correctOption);
        if(contracts.size() > 0) updatePlayersBudget(contracts,correctOption);


//        List<Bet> bets = market.getBets().stream().filter(bet -> bet.getId() != correctBetId).collect(Collectors.toList());
//        for(Bet bet : bets) {
//            List<Contract> contracts = contractRepository.findAllByBetIdAndPlayerIdIsNotNull(bet.getId());
//            if(contracts.size() > 0)updatePlayersBudget(contracts);
//
//        }


        predictionMarketRepository.update(market);

        return new PredictionMarketResponse("Rozwiązano rynek",market);

    }


    private void updatePlayersBudget(List<Contract> contracts,int correctBetId,boolean betOption) {
        for(Contract contract : contracts) {
            int countOfShares = contract.getCountOfContracts();
            if(contract.getOffers() != null)     for (SalesOffer offer : contract.getOffers()) countOfShares += offer.getCountOfContracts();
            PredictionMarket contractMarket = contract.getPredictionMarket();
            contractMarket.setCorrectBetId(correctBetId);
            setCorrectBetInfo(betOption, contract, countOfShares, contractMarket);
        }
    }

    private void updatePlayersBudget(List<Contract> contracts,boolean correctOption) {
        for(Contract contract : contracts) {
            int countOfShares = contract.getCountOfContracts();
            if(contract.getOffers() != null)    for (SalesOffer offer : contract.getOffers()) countOfShares += offer.getCountOfContracts();
            PredictionMarket contractMarket = contract.getPredictionMarket();
            setCorrectBetInfo(correctOption, contract, countOfShares, contractMarket);
        }
    }

    private void setCorrectBetInfo(boolean correctOption, Contract contract, int countOfShares, PredictionMarket contractMarket) {
        contractMarket.setCorrectBetOption(correctOption);
        contract.setPredictionMarket(contractMarket);
        contract.setOffers(null);
        contractRepository.update(contract);
        Player player = playerRepository.findByUsername(contract.getPlayerId());
        player.setBudget(player.getBudget() + countOfShares);
        playerRepository.update(player);
    }
}
