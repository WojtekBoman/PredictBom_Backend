package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Repositories.BetRepository;
import com.example.PredictBom.Repositories.PredictionMarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PredictionMarketService {

    public static final int MARKET_NOT_FOUND = 1;
    public static final int BET_ADDED = 2;


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
                marketCategory = null;
        }

        if(marketCategory == null) return new PredictionMarketResponse("Wybrano błędną kategorie",null);

//        System.out.println(counterService.getNextId("markets"));

        System.out.println(description);

        PredictionMarket predictionMarket = PredictionMarket
                .builder()
                .marketId(counterService.getNextId("markets"))
                .topic(topic)
                .category(marketCategory)
                .author(username)
                .predictedEndDate(predictedDateEnd)
                .description(description)
//                .marketCover(new Binary(BsonBinarySubType.BINARY, marketCover.getBytes()))
                .build();
        predictionMarketRepository.save(predictionMarket);


        return new PredictionMarketResponse("Utworzono nowy rynek prognostyczny",predictionMarket);
    }

        public int addBet(int id, String chosenOption){

            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);

            if(!optionalPredictionMarket.isPresent()) return  MARKET_NOT_FOUND;
            Bet newBet = Bet
                .builder()
                .id(counterService.getNextId("bets"))
                .chosenOption(chosenOption)
                .build();
            PredictionMarket predictionMarket = optionalPredictionMarket.get();
            predictionMarket.addBet(newBet);
            predictionMarketRepository.update(predictionMarket);
            betRepository.save(newBet);

            return BET_ADDED;

//            optionalPredictionMarket.get().;
        }

        public List<PredictionMarket> getAllPredictionMarkets(){
            return predictionMarketRepository.findAll();
        }

        public List<PredictionMarket> getPredictionMarketsWhereBetsIsNullByAuthor(String author) {

//        Optional<User> userOptional = userService.getUser(author);
//        if(!userOptional.isPresent()) return null;

        return predictionMarketRepository.findByBetsIsNullAndAuthor(author);
    }

        public PredictionMarket getMarketById(int id) {
            Optional<PredictionMarket> optionalPredictionMarket = predictionMarketRepository.findByMarketId(id);

            return optionalPredictionMarket.orElse(null);
        }


}
