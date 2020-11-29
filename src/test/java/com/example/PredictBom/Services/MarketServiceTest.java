package com.example.PredictBom.Services;

import com.example.PredictBom.BetRequest;
import com.example.PredictBom.Entities.Bet;
import com.example.PredictBom.Entities.MarketCategory;
import com.example.PredictBom.Entities.Player;
import com.example.PredictBom.Entities.PredictionMarket;
import com.example.PredictBom.Models.MarketWithBetsPricesResponse;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Repositories.BetRepository;
import com.example.PredictBom.Repositories.PredictionMarketRepository;
import com.example.PredictBom.Repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.swing.text.html.Option;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MarketServiceTest {

    @MockBean
    PredictionMarketRepository marketRepository;

    @MockBean
    BetRepository betRepository;

    @Autowired
    private PredictionMarketService marketService;

    @Test
    public void getPrivateMarkets() {
        List<PredictionMarket> markets = marketService.getPredictionMarketsWhereBetsIsNullByAuthor("moderator");
        assertNotNull(markets);
    }

    @Test
    public void createMarket() {
        String author = "moderator";
        String topic = "topic";
        String predictedEndDate = "3000-01-01";
        String category = "SPORT";
        String description = "description";
        PredictionMarketResponse market = marketService.createPredictionMarket(author,topic,predictedEndDate,category,description);
        assertNotNull(market.getPredictionMarket());
    }

    @Test
    public void createMarketWithExistingTopic() {
        int marketId = 1;
        String author = "moderator";
        String topic = "topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).author(author).topic(topic).description(description).predictedEndDate(predictedEndDate).category(category).build();
        when(marketRepository.findByTopic(topic)).thenReturn(Optional.of(market));
        PredictionMarketResponse responseExisting = marketService.createPredictionMarket(author,topic,category.toString(),predictedEndDate,description);
        assertNull(responseExisting.getPredictionMarket());
    }

    @Test
    public void addBet() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        int shares = 10000;
        double yesPrice = 0.5;
        double noPrice = 0.5;
        String option = "option";
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).predictedEndDate(predictedEndDate).category(category).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        BetRequest betRequest = BetRequest.builder().marketId(marketId).shares(shares).yesPrice(yesPrice).noPrice(noPrice).chosenOption(option).build();
        MarketWithBetsPricesResponse response = marketService.addBet(betRequest);
        assertNotNull(response.getBetPrice());
    }

    @Test
    public void addBetWithExistingTitle() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        int betId = 1;
        Set<Bet> bets = new HashSet<Bet>();
        int shares = 10000;
        double yesPrice = 0.5;
        double noPrice = 0.5;
        String option = "option";
        bets.add(Bet.builder().id(betId).marketId(marketId).chosenOption(option).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).predictedEndDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        BetRequest betRequest = BetRequest.builder().marketId(marketId).shares(shares).yesPrice(yesPrice).noPrice(noPrice).chosenOption(option).build();
        MarketWithBetsPricesResponse response = marketService.addBet(betRequest);
        assertNull(response.getBetPrice());
    }


    @Test
    public void deleteBet() {
        PredictionMarketResponse market = marketService.createPredictionMarket("moderator","topic","SPORT","3000-01-01","Description");
        BetRequest betRequest = BetRequest.builder().marketId(market.getPredictionMarket().getMarketId()).shares(10000).yesPrice(0.5).noPrice(0.5).chosenOption("option").build();
        when(marketRepository.findByMarketId(any(Integer.class))).thenReturn(Optional.of(market.getPredictionMarket()));
        MarketWithBetsPricesResponse response = marketService.addBet(betRequest);
        when(betRepository.findById(response.getPredictionMarket().getBets().iterator().next().getId())).thenReturn(Optional.of(response.getPredictionMarket().getBets().iterator().next()));
        PredictionMarketResponse deleteBetResponse = marketService.deleteBet(response.getPredictionMarket().getBets().iterator().next().getId());
        assertNull(deleteBetResponse.getPredictionMarket().getBets());
    }

    @Test
    public void editMarket() {
        PredictionMarketResponse market = marketService.createPredictionMarket("moderator","topic","SPORT","3000-01-01","Description");
        when(marketRepository.findByMarketId(any(Integer.class))).thenReturn(Optional.of(market.getPredictionMarket()));
        PredictionMarketResponse editResponse = marketService.editMarket(market.getPredictionMarket().getMarketId(),"editTopic","SPORT","2500-01-01","EditedDesc");
        assertEquals(editResponse.getPredictionMarket().getTopic(),"editTopic");
    }

    @Test
    public void deleteMarket() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).predictedEndDate(predictedEndDate).category(category).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        PredictionMarketResponse deleteResponse = marketService.deleteMarket(marketId);
        System.out.println(deleteResponse.getInfo());
        assertEquals(deleteResponse.getPredictionMarket(),market);
    }

    @Test
    public void deleteMarketWithBets() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betOption = "betOption";
        Set<Bet> bets = new HashSet<Bet>();
        bets.add(Bet.builder().marketId(marketId).chosenOption(betOption).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).predictedEndDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        PredictionMarketResponse deleteResponse = marketService.deleteMarket(marketId);
        System.out.println(deleteResponse.getInfo());
        assertNull(deleteResponse.getPredictionMarket());
    }

    @Test
    public void makeMarketPublished() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betOption = "betOption";
        int betId = 1;
        Set<Bet> bets = new HashSet<Bet>();
        bets.add(Bet.builder().id(betId).marketId(marketId).chosenOption(betOption).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).predictedEndDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        PredictionMarketResponse response = marketService.makeMarketPublic(marketId);
        assertTrue(response.getPredictionMarket().isPublished());
    }

    @Test
    public void makeMarketWithoutBetsPublished() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).predictedEndDate(predictedEndDate).category(category).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        PredictionMarketResponse response = marketService.makeMarketPublic(marketId);
        assertNull(response.getPredictionMarket());
    }

    @Test
    public void solveSingleBetMarket() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betOption = "betOption";
        int betId = 1;
        Set<Bet> bets = new HashSet<Bet>();
        boolean correctOption = true;
        bets.add(Bet.builder().id(betId).marketId(marketId).chosenOption(betOption).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).predictedEndDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        PredictionMarketResponse response = marketService.solveSingleBetMarket(marketId,betId,correctOption);
        assertEquals(response.getPredictionMarket().getCorrectBetId(),betId);
        assertEquals(response.getPredictionMarket().isCorrectBetOption(), correctOption);
    }

    @Test
    public void solveMultiBetMarket() {

    }

    @Test
    public void buyContract() {

    }
}
