package com.example.PredictBom.Services;

import com.example.PredictBom.Constants.MarketConstants;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BetRequest;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Repositories.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

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

    @MockBean
    ContractRepository contractRepository;

    @MockBean
    PlayerRepository playerRepository;

    @MockBean
    TransactionRepository transactionRepository;

    @Autowired
    private PredictionMarketService marketService;


    @Test
    public void createMarket() {
        String author = "moderator";
        String topic = "newTopic";
        String predictedEndDate = "3000-01-01";
        String category = "SPORT";
        String description = "description";
        ResponseEntity<?> responseEntity = marketService.createPredictionMarket(author,topic,category,predictedEndDate,description);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void createMarketWithExistingTopic() {
        int marketId = 1;
        String author = "moderator";
        String topic = "topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).author(author).topic(topic).description(description).endDate(predictedEndDate).category(category).build();
        when(marketRepository.findByTopic(topic)).thenReturn(Optional.of(market));
        ResponseEntity<?> responseExisting = marketService.createPredictionMarket(author,topic,category.toString(),predictedEndDate,description);
        assertEquals(responseExisting.getBody(), MarketConstants.MARKET_EXISTING_INFO);
    }

    @Test
    public void addBet() {
        int marketId = 1;
        String topic = "topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        int shares = 10000;
        double yesPrice = 0.5;
        double noPrice = 0.5;
        String option = "option";
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        BetRequest betRequest = BetRequest.builder().marketId(marketId).shares(shares).yesPrice(yesPrice).noPrice(noPrice).title(option).build();
        ResponseEntity<?> response = marketService.addBet(betRequest);
        assertEquals(response.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void addBetWithExistingTitle() {
        int marketId = 1;
        String topic = "topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        int betId = 1;
        Set<Bet> bets = new HashSet<>();
        int shares = 10000;
        double yesPrice = 0.5;
        double noPrice = 0.5;
        String option = "option";
        bets.add(Bet.builder().id(betId).marketId(marketId).title(option).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        BetRequest betRequest = BetRequest.builder().marketId(marketId).shares(shares).yesPrice(yesPrice).noPrice(noPrice).title(option).build();
        ResponseEntity<?> response = marketService.addBet(betRequest);
        assertEquals(response.getBody(),MarketConstants.BET_EXISTING_INFO);
    }


    @Test
    public void editMarket() {
        int marketId = 1;
        String author = "moderator";
        String topic = "topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        PredictionMarket market = PredictionMarket.builder()
                .marketId(marketId)
                .author(author)
                .topic(topic)
                .category(category)
                .endDate(predictedEndDate)
                .description(description)
                .build();
        when(marketRepository.findByMarketId(any(Integer.class))).thenReturn(Optional.of(market));
        ResponseEntity<?> editResponse = marketService.editMarket(market.getMarketId(),"editTopic","SPORT","2500-01-01","EditedDesc");
        assertEquals(editResponse.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void deleteMarket() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        ResponseEntity<?> deleteResponse = marketService.deleteMarket(marketId);
        assertEquals(deleteResponse.getBody(),market);
    }
//
    @Test
    public void deleteMarketWithBets() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betOption = "betOption";
        Set<Bet> bets = new HashSet<>();
        bets.add(Bet.builder().marketId(marketId).title(betOption).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        ResponseEntity<?> deleteResponse = marketService.deleteMarket(marketId);
        assertEquals(deleteResponse.getBody(),MarketConstants.FIRST_DELETE_BET_INFO);
    }

    @Test
    public void makeMarketPublished() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betTitle = "betTitle";
        int betId = 1;
        Set<Bet> bets = new HashSet<>();
        bets.add(Bet.builder().id(betId).marketId(marketId).title(betTitle).build());
        PredictionMarket market =
                PredictionMarket.builder()
                        .marketId(marketId)
                        .topic(topic)
                        .description(description)
                        .endDate(predictedEndDate)
                        .category(category)
                        .bets(bets)
                        .build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        ResponseEntity<?> response = marketService.makeMarketPublic(marketId);
        assertEquals(response.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void makeMarketWithoutBetsPublished() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        PredictionMarket market = PredictionMarket.builder()
                .marketId(marketId)
                .topic(topic)
                .description(description)
                .endDate(predictedEndDate)
                .category(category)
                .build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        ResponseEntity<?> response = marketService.makeMarketPublic(marketId);
        assertEquals(response.getBody(),MarketConstants.FIRST_ADD_BETS_INFO);
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
        Set<Bet> bets = new HashSet<>();
        Bet bet = Bet.builder().id(betId).marketId(marketId).title(betOption).build();
        bets.add(bet);
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        int budget = 1000;
        Player player = new Player(username,firstName,surname,email,password,budget);


        int contractId = 1;
        int contractShares = 100;
        Contract contract = Contract.builder().id(contractId).playerId(username).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        List<Contract> contracts = new ArrayList<>();
        contracts.add(contract);
        when(contractRepository.findAllByBetIdAndPlayerIdIsNotNull(betId)).thenReturn(contracts);

        when(playerRepository.findByUsername(contract.getPlayerId())).thenReturn(player);

        ResponseEntity<?> response = marketService.solveMarket(marketId,betId, true);
        assertEquals(response.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void solveMultiBetMarket() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String firstBetOption = "firstBetOption";
        String secondBetOption = "secondBetOption";
        int firstBetId = 1;
        int secondBetId = 2;
        Set<Bet> bets = new HashSet<>();
        bets.add(Bet.builder().id(firstBetId).marketId(marketId).title(firstBetOption).build());
        bets.add(Bet.builder().id(secondBetId).marketId(marketId).title(secondBetOption).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        ResponseEntity<?> marketResponse = marketService.solveMarket(marketId,firstBetId, true);
        assertEquals(marketResponse.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void buyContractNotFoundOffers() {

        int betId = 1;
        int marketId = 1;

        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        int budget = 1000;
        Player player = new Player(username,firstName,surname,email,password,budget);

        when(playerRepository.findByUsername(username)).thenReturn(player);

        ResponseEntity<?> response = marketService.buyContract(username,betId,marketId,true,100,1);
        assertEquals(response.getBody(),MarketConstants.NOT_FOUND_OFFERS_INFO);
    }


    @Test
    public void buyContract() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betOption = "betOption";
        int betId = 1;
        Set<Bet> bets = new HashSet<>();
        Bet bet = Bet.builder().id(betId).marketId(marketId).title(betOption).build();
        bets.add(bet);
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        int budget = 1000;
        Player player = new Player(username,firstName,surname,email,password,budget);


        int contractId = 1;
        int contractShares = 100;
        Contract contract = Contract.builder().id(contractId).playerId(username).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        List<Contract> contracts = new ArrayList<>();
        int offerId = 1;
        Offer offer = Offer.builder().contractId(contractId).id(offerId).shares(50).price(0.5).build();
        contract.addOffer(offer);
        contracts.add(contract);

        when(contractRepository.findById(offer.getContractId())).thenReturn(Optional.of(contract));
        when(contractRepository.findOffersToBuy(betId, true,username)).thenReturn(contracts);
        when(playerRepository.findByUsername(contract.getPlayerId())).thenReturn(player);
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        ResponseEntity<?> response = marketService.buyContract(username,betId,marketId, true,50,1);
        assertEquals(response.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void buyContractTooLowBudget() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betOption = "betOption";
        int betId = 1;
        Set<Bet> bets = new HashSet<>();
        Bet bet = Bet.builder().id(betId).marketId(marketId).title(betOption).build();
        bets.add(bet);
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        int budget = 10;
        Player player = new Player(username,firstName,surname,email,password,budget);


        int contractId = 1;
        int contractShares = 100;
        Contract contract = Contract.builder().id(contractId).playerId(username).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        List<Contract> contracts = new ArrayList<>();
        int offerId = 1;
        Offer offer = Offer.builder().contractId(contractId).id(offerId).shares(50).price(0.5).build();
        contract.addOffer(offer);
        contracts.add(contract);

        when(contractRepository.findById(offer.getContractId())).thenReturn(Optional.of(contract));
        when(contractRepository.findOffersToBuy(betId, true,username)).thenReturn(contracts);
        when(playerRepository.findByUsername(contract.getPlayerId())).thenReturn(player);
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        ResponseEntity<?> response = marketService.buyContract(username,betId,marketId, true,50,1);
        assertEquals(response.getBody(),MarketConstants.LOW_BUDGET_INFO);
    }

    @Test
    public void buyContractNotFoundShares() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betOption = "betOption";
        int betId = 1;
        Set<Bet> bets = new HashSet<>();
        Bet bet = Bet.builder().id(betId).marketId(marketId).title(betOption).build();
        bets.add(bet);
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        int budget = 1000;
        Player player = new Player(username,firstName,surname,email,password,budget);


        int contractId = 1;
        int contractShares = 100;
        Contract contract = Contract.builder().id(contractId).playerId(username).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        List<Contract> contracts = new ArrayList<>();
        int offerId = 1;
        Offer offer = Offer.builder().contractId(contractId).id(offerId).shares(50).price(0.5).build();
        contract.addOffer(offer);
        contracts.add(contract);

        when(contractRepository.findById(offer.getContractId())).thenReturn(Optional.of(contract));
        when(contractRepository.findOffersToBuy(betId, true,username)).thenReturn(contracts);
        when(playerRepository.findByUsername(contract.getPlayerId())).thenReturn(player);
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        ResponseEntity<?> response = marketService.buyContract(username,betId,marketId, true,100,1);
        assertEquals(response.getBody(),"Znaleziono " + 50 + " spełniających podane kryteria");
    }

    @Test
    public void buySharesToExistingContract() {
        int marketId = 1;
        String topic = "Topic";
        String predictedEndDate = "3000-01-01";
        MarketCategory category = MarketCategory.SPORT;
        String description = "description";
        String betOption = "betOption";
        int betId = 1;
        Set<Bet> bets = new HashSet<>();
        Bet bet = Bet.builder().id(betId).marketId(marketId).title(betOption).build();
        bets.add(bet);
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        int budget = 1000;
        Player player = new Player(username,firstName,surname,email,password,budget);


        int contractId = 1;
        int contractShares = 100;
        Contract contractToBuy = Contract.builder().id(contractId).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        Contract userContract = Contract.builder().id(contractId).playerId(username).bet(bet).shares(contractShares).contractStatus(ContractStatus.PENDING).build();
        List<Contract> contracts = new ArrayList<>();
        int offerId = 1;
        Offer offer = Offer.builder().contractId(contractId).id(offerId).shares(50).price(0.5).build();
        contractToBuy.addOffer(offer);
        contracts.add(contractToBuy);

        when(contractRepository.findById(offer.getContractId())).thenReturn(Optional.of(contractToBuy));
        when(contractRepository.findOffersToBuy(betId, true,username)).thenReturn(contracts);
        when(contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId, true)).thenReturn(Optional.of(userContract));
        when(playerRepository.findByUsername(username)).thenReturn(player);
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        ResponseEntity<?> response = marketService.buyContract(username,betId,marketId, true,50,1);
        BuyContractResponse buyResponse = (BuyContractResponse) response.getBody();
        assert buyResponse != null;
        assertEquals(buyResponse.getBoughtContract().getShares(),150);
    }



}
