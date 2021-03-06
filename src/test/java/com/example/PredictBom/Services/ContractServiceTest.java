package com.example.PredictBom.Services;

import com.example.PredictBom.Constants.ContractConstants;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.AddOfferRequest;
import com.example.PredictBom.Repositories.ContractRepository;
import com.example.PredictBom.Repositories.SalesOfferRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractServiceTest {

    @MockBean
    ContractRepository contractRepository;

    @MockBean
    SalesOfferRepository salesOfferRepository;

    @Autowired
    private ContractService contractService;

    @Test
    public void addOffer() {
        int contractId = 1;
        String player = "player";
        int contractShares = 100;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).playerId(player).shares(contractShares).build();
        AddOfferRequest request = AddOfferRequest.builder().contractId(contractId).shares(sellShares).price(sellPrice).build();
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        ResponseEntity<?> contractResponse = contractService.addOfferFromContract(player,request);
        assertEquals(contractResponse.getBody(),contract );
    }

    @Test
    public void addOfferFromContractWithoutShares() {
        int contractId = 1;
        String player = "player";
        int contractShares = 0;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).playerId(player).shares(contractShares).build();
        AddOfferRequest request = AddOfferRequest.builder().contractId(contractId).shares(sellShares).price(sellPrice).build();
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        ResponseEntity<?> contractResponse = contractService.addOfferFromContract(player, request);
        assertEquals(contractResponse.getBody(), ContractConstants.NOT_ENOUGH_SHARES_INFO);
    }

    @Test
    public void deleteOffer() {
        int contractId = 1;
        int offerId = 1;
        String player = "player";
        int contractShares = 100;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).playerId(player).shares(contractShares).build();
        Offer salesOffer = Offer.builder().id(offerId).contractId(contractId).price(sellPrice).shares(sellShares).build();
        contract.addOffer(salesOffer);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(salesOfferRepository.findById(offerId)).thenReturn(Optional.of(salesOffer));
        ResponseEntity<?> contractResponse = contractService.deleteOfferFromContract(player,offerId);
        assertEquals(contractResponse.getBody(),contract);
    }

    @Test
    public void deleteSoldOffer() {
        int contractId = 1;
        int offerId = 1;
        String player = "player";
        int contractShares = 100;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).playerId(player).shares(contractShares).build();
        Offer salesOffer = Offer.builder().id(offerId).contractId(contractId).price(sellPrice).shares(sellShares).build();
        contract.addOffer(salesOffer);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        ResponseEntity<?> contractResponse = contractService.deleteOfferFromContract(player,offerId);
        assertEquals(contractResponse.getBody(),ContractConstants.OFFER_IS_NOT_FOUND_INFO);
    }

    @Test
    public void getContractById() {
        Contract contract = Contract.builder().id(1).playerId("User").build();
        when(contractRepository.findByIdAndPlayerId(1,"User")).thenReturn(Optional.of(contract));
        ResponseEntity<?> response = contractService.getContractById(1,"User");
        assertEquals(response.getBody(),contract);
    }

    @Test
    public void getContractByIdNotFound() {
        ResponseEntity<?> response = contractService.getContractById(1,"User");
        assertEquals(response.getBody(),ContractConstants.CONTRACT_NOT_FOUND_INFO);
    }

//    @Test
//    public void getPlayerContractsByOption() {
//        List<Contract> contracts = new ArrayList<>();
//
//        contracts.add(
//                Contract.builder().playerId("User").id(1).shares(20).bet(Bet.builder().title("Sportowy zakład").build())
//                .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build()).build()
//        );
//
//        contracts.add(
//                Contract.builder().playerId("User").id(2).shares(20).bet(Bet.builder().title("Sportowy zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build()).build()
//        );
//
//        contracts.add(
//                Contract.builder().playerId("User").id(3).shares(20).bet(Bet.builder().title("Sportowy zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build()).build()
//        );
//        String[] categories = new String[0];
//
//        when(contractRepository.findByPlayerIdAndContractOption("User",true, Sort.by(Sort.Direction.fromString("desc"),"modifiedDate"))).thenReturn(contracts);
//        List<Contract> testContracts = contractService.getPlayerContractsByOption("User","",true,"","",categories,"modifiedDate","desc");
//        assertEquals(testContracts.size(),contracts.size());
//    }
//
//    @Test
//    public void getFilteredPlayerContractsByOption() {
//        List<Contract> contracts = new ArrayList<>();
//
//        contracts.add(
//                Contract.builder().playerId("User").id(1).shares(20).bet(Bet.builder().title("Sportowy zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build()).build()
//        );
//
//        contracts.add(
//                Contract.builder().playerId("User").id(2).shares(20).bet(Bet.builder().title("Ekonomiczny zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Ekonomiczny temat").marketCategory(MarketCategory.ECONOMY).build()).build()
//        );
//
//        contracts.add(
//                Contract.builder().playerId("User").id(3).shares(20).bet(Bet.builder().title("Popularny zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build()).build()
//        );
//        String[] categories = {"ECONOMY","SPORT"};
//
//        when(contractRepository.findByPlayerIdAndContractOption("User",true, Sort.by(Sort.Direction.fromString("desc"),"modifiedDate"))).thenReturn(contracts);
//        List<Contract> testContracts = contractService.getPlayerContractsByOption("User","",true,"","",categories,"modifiedDate","desc");
//        assertEquals(testContracts.size(),2);
//    }
//
//    @Test
//    public void getPlayerContracts() {
//        List<Contract> contracts = new ArrayList<>();
//
//        contracts.add(
//                Contract.builder().playerId("User").id(1).shares(20).bet(Bet.builder().title("Sportowy zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build()).build()
//        );
//
//        contracts.add(
//                Contract.builder().playerId("User").id(2).shares(20).bet(Bet.builder().title("Ekonomiczny zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Ekonomiczny temat").marketCategory(MarketCategory.ECONOMY).build()).build()
//        );
//
//        contracts.add(
//                Contract.builder().playerId("User").id(3).shares(20).bet(Bet.builder().title("Popularny zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build()).build()
//        );
//        String[] categories = new String[0];
//
//        when(contractRepository.findNotSolvedMarketsByPlayerId("User",Sort.by(Sort.Direction.fromString("desc"),"modifiedDate"))).thenReturn(contracts);
//        List<Contract> testContracts = contractService.getFilteredPlayerContracts("User","","","",categories,"modifiedDate","desc");
//        assertEquals(testContracts.size(),3);
//    }
//
//    @Test
//    public void getFilteredPlayerContracts() {
//        List<Contract> contracts = new ArrayList<>();
//
//        contracts.add(
//                Contract.builder().playerId("User").id(1).shares(20).bet(Bet.builder().title("Sportowy zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build()).build()
//        );
//
//        contracts.add(
//                Contract.builder().playerId("User").id(2).shares(20).bet(Bet.builder().title("Ekonomiczny zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Ekonomiczny temat").marketCategory(MarketCategory.ECONOMY).build()).build()
//        );
//
//        contracts.add(
//                Contract.builder().playerId("User").id(3).shares(20).bet(Bet.builder().title("Popularny zakład").build())
//                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build()).build()
//        );
//        String[] categories = {"ECONOMY","SPORT"};
//
//        when(contractRepository.findNotSolvedMarketsByPlayerId("User",Sort.by(Sort.Direction.fromString("desc"),"modifiedDate"))).thenReturn(contracts);
//        List<Contract> testContracts = contractService.getFilteredPlayerContracts("User","","","",categories,"modifiedDate","desc");
//        assertEquals(testContracts.size(),2);
//    }

}
