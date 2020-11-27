package com.example.PredictBom.Services;

import com.example.PredictBom.BetRequest;
import com.example.PredictBom.Entities.PredictionMarket;
import com.example.PredictBom.Models.MarketWithBetsPricesResponse;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Repositories.PredictionMarketRepository;
import com.example.PredictBom.Repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MarketServiceTest {

    @MockBean
    PredictionMarketRepository marketRepository;

    @Autowired
    private PredictionMarketService marketService;

    @Test
    public void getPrivateMarkets() {
        List<PredictionMarket> markets = marketService.getPredictionMarketsWhereBetsIsNullByAuthor("moderator");
        assertNotNull(markets);
    }

    @Test
    public void createMarket() {
        PredictionMarketResponse market = marketService.createPredictionMarket("moderator","XD","SPORT","XD","XD");
        assertNotNull(market.getPredictionMarket());
    }

    @Test
    public void addBet() {
        PredictionMarketResponse market = marketService.createPredictionMarket("moderator","XD","SPORT","XD","XD");

        BetRequest betRequest = BetRequest.builder().marketId(market.getPredictionMarket().getMarketId()).shares(10000).yesPrice(0.5).noPrice(0.5).chosenOption("option").build();
        when(marketRepository.findByMarketId(any(Integer.class))).thenReturn(Optional.of(market.getPredictionMarket()));
        MarketWithBetsPricesResponse response = marketService.addBet(betRequest);

        assertNotNull(response.getBetPrice());
    }

    @Test
    public void deleteBet() {
        
    }

    public void getMarkets() {

    }
}
