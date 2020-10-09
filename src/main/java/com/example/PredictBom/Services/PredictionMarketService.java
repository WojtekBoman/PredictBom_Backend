package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Repositories.BetRepository;
import com.example.PredictBom.Repositories.PredictionMarketRepository;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    PredictionMarketRepository predictionMarketRepository;

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

        public PredictionMarketResponse addBet(int id, String chosenOption){

            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);

            if(!optionalPredictionMarket.isPresent()) return new PredictionMarketResponse("Nie znaleziono rynku prognostycznego",null);
            Bet newBet = Bet
                .builder()
                .id(counterService.getNextId("bets"))
                .chosenOption(chosenOption)
                .build();
            PredictionMarket predictionMarket = optionalPredictionMarket.get();
            predictionMarket.addBet(newBet);
            predictionMarketRepository.update(predictionMarket);
            betRepository.save(newBet);

            return new PredictionMarketResponse("Dodano zakład",predictionMarket);

//            optionalPredictionMarket.get().;
        }

        public PredictionMarketResponse deleteBet(int marketId, int betId) {
            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(marketId);
            if(!optionalPredictionMarket.isPresent()) return new PredictionMarketResponse("Nie znaleziono takiego rynku",null);

            Optional<Bet> optionalBet = betRepository.findBetById(betId);
            if(!optionalBet.isPresent()) return new PredictionMarketResponse("Nie znaleziono takiego zakładu",null);

            PredictionMarket marketToDeleteBet = optionalPredictionMarket.get();
            marketToDeleteBet.deleteBet(betId);

            predictionMarketRepository.update(marketToDeleteBet);
            betRepository.deleteBetById(betId);

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

        public PredictionMarketResponse setMarketCover(int id, MultipartFile marketCover) throws IOException {
            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);

            if(!optionalPredictionMarket.isPresent()) return new PredictionMarketResponse("Nie znaleziono rynku prognostycznego",null);

            PredictionMarket predictionMarket = optionalPredictionMarket.get();
            predictionMarket.setMarketCover(new Binary(BsonBinarySubType.BINARY, marketCover.getBytes()));
            predictionMarketRepository.update(predictionMarket);

            return new PredictionMarketResponse("Zmieniono okładkę",predictionMarket);
        }

        public List<PredictionMarket> getFilteredMarketsWaitingForBets(String username, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {

            Optional<User> userOptional = userService.getUser(username);
            if(!userOptional.isPresent()) return null;

            List<PredictionMarket> predictionMarketsList = new ArrayList<>(predictionMarketRepository.findByBetsIsNullAndAuthor(username, Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute)));
            return getPredictionMarkets(marketTitle, marketCategory, predictionMarketsList);
        }

        public List<PredictionMarket> getFilteredPrivateMarkets(String username, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection){
            Optional<User> userOptional = userService.getUser(username);
            if(!userOptional.isPresent()) return null;

            List<PredictionMarket> predictionMarketsList = new ArrayList<>(predictionMarketRepository.findByPublishedFalse(username,Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute)));

            return getPredictionMarkets(marketTitle, marketCategory, predictionMarketsList);
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


}
