package com.example.PredictBom.Services;

import com.example.PredictBom.Constants.OfferConstants;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Repositories.*;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OfferServiceTest {

    @MockBean
    SalesOfferRepository offerRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    PlayerRepository playerRepository;

    @MockBean
    ContractRepository contractRepository;

    @MockBean
    PredictionMarketRepository marketRepository;

    @Autowired
    private OfferService offerService;


    @BeforeEach
    @AfterEach
    public void resetMock() {
        Mockito.reset(offerService);
    }



    @Test
    public void buyShares() {
        //initialize user
        String username = "NewUser";
        String dealerUsername = "Dealer";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        double budget = 1000;
        Player player = new Player(username,firstName,surname,email,password,budget);


        //initialize market and bet
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

        //initialize contract and offer to buy
        int contractId = 1;
        int offerId = 1;
        int contractShares = 100;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).bet(bet).playerId(dealerUsername).shares(contractShares).build();
        Offer salesOffer = Offer.builder().id(offerId).contractId(contractId).price(sellPrice).shares(sellShares).build();

        int buyShares = 10;

        when(playerRepository.findByUsername(player.getUsername())).thenReturn(player);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(salesOffer));
        when(contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId,true)).thenReturn(Optional.empty());

        ResponseEntity<?> buyContractResponse = offerService.buyShares(username,offerId,buyShares);
        BuyContractResponse boughtContract = (BuyContractResponse) buyContractResponse.getBody();
        assert boughtContract != null;
        assertEquals(boughtContract.getBoughtContract().getShares(),10);
    }


    @Test
    public void buySharesTooLowBudget() {
        //initialize user
        String username = "user";
        String dealerUsername = "Dealer";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        double budget = 5;
        Player player = new Player(username,firstName,surname,email,password,budget);
        System.out.println(player.getBudget());

        //initialize market and bet
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

        //initialize contract and offer to buy
        int contractId = 1;
        int offerId = 1;
        int contractShares = 1000;
        int sellShares = 500;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).bet(bet).playerId(dealerUsername).shares(contractShares).build();
        Offer salesOffer = Offer.builder().id(offerId).contractId(contractId).price(sellPrice).shares(sellShares).build();

        int buyShares = 100;

        when(playerRepository.findByUsername(username)).thenReturn(player);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(salesOffer));
        when(contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId,true)).thenReturn(Optional.empty());

        ResponseEntity<?> buyContractResponse = offerService.buyShares(username,offerId,buyShares);
        assertEquals(buyContractResponse.getBody(), OfferConstants.NOT_ENOUGH_MONEY_INFO);
    }

    @Test
    public void buySharesToExistingContract() {
        String username = "NewUser2";
        String dealerUsername = "Dealer";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        double budget = 1000;
        Player player = new Player(username,firstName,surname,email,password,budget);


        //initialize market and bet
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

        //initialize contract and offer to buy
        int contractId = 1;
        int offerId = 1;
        int contractShares = 100;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).bet(bet).playerId(dealerUsername).shares(contractShares).contractOption(true).build();
        Offer salesOffer = Offer.builder().id(offerId).contractId(contractId).price(sellPrice).shares(sellShares).build();

        int buyShares = 10;

        int userContractId = 2;
        int userContractShares = 30;
        Contract userContract = Contract.builder().id(userContractId).bet(bet).playerId(username).shares(userContractShares).contractOption(true).build();

        when(playerRepository.findByUsername(player.getUsername())).thenReturn(player);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(salesOffer));
        when(contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId,true)).thenReturn(Optional.of(userContract));

        ResponseEntity<?> response = offerService.buyShares(username,offerId,buyShares);
        BuyContractResponse buyContractResponse = (BuyContractResponse) response.getBody();
        assert buyContractResponse != null;
        assertEquals(buyContractResponse.getBoughtContract().getShares(),userContractShares + buyShares);
        assertEquals(buyContractResponse.getBoughtContract().getId(),userContractId);
    }
}
