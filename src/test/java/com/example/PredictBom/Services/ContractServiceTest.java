package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.SalesOffer;
import com.example.PredictBom.Models.AddOfferRequest;
import com.example.PredictBom.Models.ContractResponse;
import com.example.PredictBom.Repositories.ContractRepository;
import com.example.PredictBom.Repositories.SalesOfferRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
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
        Contract contract = Contract.builder().id(contractId).playerId(player).countOfContracts(contractShares).build();
        AddOfferRequest request = AddOfferRequest.builder().contractId(contractId).countOfShares(50).sellPrice(sellPrice).build();
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        ContractResponse contractResponse = contractService.addOffer(player,request);
        assertEquals(contractResponse.getContract().getCountOfContracts(),contractShares-sellShares);
        assertEquals(contractResponse.getContract().getOffers().iterator().next().getCountOfContracts(),sellShares);
        assertNotNull(contractResponse.getContract().getOffers());
    }

    @Test
    public void addOfferFromContractWithoutShares() {
        int contractId = 1;
        String player = "player";
        int contractShares = 0;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).playerId(player).countOfContracts(contractShares).build();
        AddOfferRequest request = AddOfferRequest.builder().contractId(contractId).countOfShares(sellShares).sellPrice(sellPrice).build();
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        ContractResponse contractResponse = contractService.addOffer(player, request);
        assertNull(contractResponse.getContract());
    }

    @Test
    public void deleteOffer() {
        int contractId = 1;
        int offerId = 1;
        String player = "player";
        int contractShares = 100;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).playerId(player).countOfContracts(contractShares).build();
        SalesOffer salesOffer = SalesOffer.builder().id(offerId).contractId(contractId).valueOfShares(sellPrice).countOfContracts(sellShares).build();
        contract.addOffer(salesOffer);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(salesOfferRepository.findById(offerId)).thenReturn(Optional.of(salesOffer));
        ContractResponse contractResponse = contractService.deleteOffer(player,offerId);
        assertNull(contractResponse.getContract().getOffers());
        assertEquals(contractResponse.getContract().getCountOfContracts(),contractShares+sellShares);
    }

    @Test
    public void deleteSoldOffer() {
        int contractId = 1;
        int offerId = 1;
        String player = "player";
        int contractShares = 100;
        int sellShares = 50;
        double sellPrice = 0.5;
        Contract contract = Contract.builder().id(contractId).playerId(player).countOfContracts(contractShares).build();
        SalesOffer salesOffer = SalesOffer.builder().id(offerId).contractId(contractId).valueOfShares(sellPrice).countOfContracts(sellShares).build();
        contract.addOffer(salesOffer);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        ContractResponse contractResponse = contractService.deleteOffer(player,offerId);
        assertNull(contractResponse.getContract());
    }

}
