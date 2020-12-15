package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BetRequest;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Models.MarketWithBetsPricesResponse;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Repositories.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
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
    public void getPrivateMarkets() {
        List<PredictionMarket> markets = marketService.getPredictionMarketsWhereBetsIsNullByAuthor("moderator");
        assertNotNull(markets);
    }

    @Test
    public void createMarket() {
        String author = "moderator";
        String topic = "newTopic";
        String predictedEndDate = "3000-01-01";
        String category = "SPORT";
        String description = "description";
        PredictionMarketResponse market = marketService.createPredictionMarket(author,topic,category,predictedEndDate,description);
        System.out.println(market.getInfo());
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
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).author(author).topic(topic).description(description).endDate(predictedEndDate).category(category).build();
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
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        BetRequest betRequest = BetRequest.builder().marketId(marketId).shares(shares).yesPrice(yesPrice).noPrice(noPrice).title(option).build();
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
        bets.add(Bet.builder().id(betId).marketId(marketId).title(option).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        BetRequest betRequest = BetRequest.builder().marketId(marketId).shares(shares).yesPrice(yesPrice).noPrice(noPrice).title(option).build();
        MarketWithBetsPricesResponse response = marketService.addBet(betRequest);
        assertNull(response.getBetPrice());
    }


    @Test
    public void deleteBet() {
        PredictionMarketResponse market = marketService.createPredictionMarket("moderator","topic","SPORT","3000-01-01","Description");
        BetRequest betRequest = BetRequest.builder().marketId(market.getPredictionMarket().getMarketId()).shares(10000).yesPrice(0.5).noPrice(0.5).title("option").build();
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
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).build();
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
        bets.add(Bet.builder().marketId(marketId).title(betOption).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
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
        PredictionMarket market = PredictionMarket.builder()
                .marketId(marketId)
                .topic(topic)
                .description(description)
                .endDate(predictedEndDate)
                .category(category)
                .build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        PredictionMarketResponse response = marketService.makeMarketPublic(marketId);
        assertEquals(response.getInfo(),PredictionMarketService.NOT_FOUND_BETS_INFO);
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

        PredictionMarketResponse response = marketService.solveSingleBetMarket(marketId,betId,correctOption);
        assertEquals(response.getPredictionMarket().getCorrectBetId(),betId);
        assertEquals(response.getPredictionMarket().isCorrectBetOption(), correctOption);
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
        Set<Bet> bets = new HashSet<Bet>();
        boolean correctOption = true;
        bets.add(Bet.builder().id(firstBetId).marketId(marketId).title(firstBetOption).build());
        bets.add(Bet.builder().id(secondBetId).marketId(marketId).title(secondBetOption).build());
        PredictionMarket market = PredictionMarket.builder().marketId(marketId).topic(topic).description(description).endDate(predictedEndDate).category(category).bets(bets).build();
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));
        PredictionMarketResponse marketResponse = marketService.solveMultiBetMarket(marketId,firstBetId);
        assertEquals(marketResponse.getPredictionMarket().getCorrectBetId(),firstBetId);
    }


    @Test
    public void filterMarketsByCategory() {
        List<PredictionMarket> markets = new ArrayList<PredictionMarket>();

        markets.add(
                PredictionMarket.builder().author("moderator").marketId(1).category(MarketCategory.SPORT).topic("Topic1").build()
        );

        markets.add(
                PredictionMarket.builder().author("moderator").marketId(2).category(MarketCategory.CELEBRITIES).topic("Topic2").build()
        );

        markets.add(
                PredictionMarket.builder().author("moderator").marketId(3).category(MarketCategory.POLICY).topic("Topic3").build()
        );

        when(marketRepository.findPublishedNotSolvedMarkets(Sort.by(Sort.Direction.fromString("desc"),"createdDate"))).thenReturn(markets);
        String[] categories = {"SPORT"};
        List<PredictionMarket> filteredMarkets = marketService.getPublicMarkets("",categories,"createdDate","desc");
        assertEquals(1,filteredMarkets.size());
    }

    @Test
    public void filterMarketsByTitle() {
        List<PredictionMarket> markets = new ArrayList<PredictionMarket>();

        markets.add(
                PredictionMarket.builder().author("moderator").marketId(1).category(MarketCategory.SPORT).topic("Topic1").build()
        );

        markets.add(
                PredictionMarket.builder().author("moderator").marketId(2).category(MarketCategory.SPORT).topic("Topic2").build()
        );

        markets.add(
                PredictionMarket.builder().author("moderator").marketId(3).category(MarketCategory.SPORT).topic("Another").build()
        );

        when(marketRepository.findPublishedNotSolvedMarkets(Sort.by(Sort.Direction.fromString("desc"),"createdDate"))).thenReturn(markets);
        String[] categories = {"SPORT"};
        List<PredictionMarket> filteredMarkets = marketService.getPublicMarkets("Topic",categories,"createdDate","desc");
        assertEquals(2,filteredMarkets.size());
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

        BuyContractResponse response = marketService.buyContract(username,betId,marketId,true,100,1);
        assertNull(response.getBoughtContract());
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
        Set<Bet> bets = new HashSet<Bet>();
        boolean correctOption = true;
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
        boolean option = true;
        Contract contract = Contract.builder().id(contractId).playerId(username).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        List<Contract> contracts = new ArrayList<>();
        int offerId = 1;
        Offer offer = Offer.builder().contractId(contractId).id(offerId).shares(50).price(0.5).build();
        contract.addOffer(offer);
        contracts.add(contract);

        when(contractRepository.findById(offer.getContractId())).thenReturn(Optional.of(contract));
        when(contractRepository.findOffersToBuy(betId,option,username)).thenReturn(contracts);
        when(playerRepository.findByUsername(contract.getPlayerId())).thenReturn(player);
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        BuyContractResponse response = marketService.buyContract(username,betId,marketId,option,50,1);
        assertNotNull(response.getBoughtContract());
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
        Set<Bet> bets = new HashSet<Bet>();
        boolean correctOption = true;
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
        boolean option = true;
        Contract contract = Contract.builder().id(contractId).playerId(username).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        List<Contract> contracts = new ArrayList<>();
        int offerId = 1;
        Offer offer = Offer.builder().contractId(contractId).id(offerId).shares(50).price(0.5).build();
        contract.addOffer(offer);
        contracts.add(contract);

        when(contractRepository.findById(offer.getContractId())).thenReturn(Optional.of(contract));
        when(contractRepository.findOffersToBuy(betId,option,username)).thenReturn(contracts);
        when(playerRepository.findByUsername(contract.getPlayerId())).thenReturn(player);
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        BuyContractResponse response = marketService.buyContract(username,betId,marketId,option,50,1);
        System.out.println(response.getInfo());
        assertNull(response.getBoughtContract());
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
        Set<Bet> bets = new HashSet<Bet>();
        boolean correctOption = true;
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
        boolean option = true;
        Contract contract = Contract.builder().id(contractId).playerId(username).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        List<Contract> contracts = new ArrayList<>();
        int offerId = 1;
        Offer offer = Offer.builder().contractId(contractId).id(offerId).shares(50).price(0.5).build();
        contract.addOffer(offer);
        contracts.add(contract);

        when(contractRepository.findById(offer.getContractId())).thenReturn(Optional.of(contract));
        when(contractRepository.findOffersToBuy(betId,option,username)).thenReturn(contracts);
        when(playerRepository.findByUsername(contract.getPlayerId())).thenReturn(player);
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        BuyContractResponse response = marketService.buyContract(username,betId,marketId,option,100,1);
        System.out.println(response.getInfo());
        assertNull(response.getBoughtContract());
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
        Set<Bet> bets = new HashSet<Bet>();
        boolean correctOption = true;
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
        boolean option = true;
        Contract contractToBuy = Contract.builder().id(contractId).contractStatus(ContractStatus.PENDING).shares(contractShares).bet(bet).build();
        Contract userContract = Contract.builder().id(contractId).playerId(username).bet(bet).shares(contractShares).contractStatus(ContractStatus.PENDING).build();
        List<Contract> contracts = new ArrayList<>();
        int offerId = 1;
        Offer offer = Offer.builder().contractId(contractId).id(offerId).shares(50).price(0.5).build();
        contractToBuy.addOffer(offer);
        contracts.add(contractToBuy);

        when(contractRepository.findById(offer.getContractId())).thenReturn(Optional.of(contractToBuy));
        when(contractRepository.findOffersToBuy(betId,option,username)).thenReturn(contracts);
        when(contractRepository.findByPlayerIdAndBetIdAndContractOption(username,betId,option)).thenReturn(Optional.of(userContract));
        when(playerRepository.findByUsername(username)).thenReturn(player);
        when(marketRepository.findByMarketId(marketId)).thenReturn(Optional.of(market));

        BuyContractResponse response = marketService.buyContract(username,betId,marketId,option,50,1);
        System.out.println(response.getInfo());
        assertEquals(response.getBoughtContract().getShares(),150);
    }



}
