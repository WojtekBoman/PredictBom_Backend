package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.Bet;
import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.PredictionMarket;
import com.example.PredictBom.Entities.SalesOffer;
import com.example.PredictBom.Models.ContractDetailsResponse;
import com.example.PredictBom.Repositories.BetRepository;
import com.example.PredictBom.Repositories.ContractRepository;
import com.example.PredictBom.Repositories.PredictionMarketRepository;
import com.example.PredictBom.Repositories.SalesOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContractService {

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    SalesOfferRepository salesOfferRepository;

    @Autowired
    BetRepository betRepository;

    @Autowired
    CounterService counterService;

    @Autowired
    PredictionMarketRepository predictionMarketRepository;

    public List<Contract> getPlayerContracts(String username) {
        return contractRepository.findByPlayerId(username);
    }

    @Transactional
    public Contract addOffer(int contractId, int countOfShares, int sellPrice) {

        Contract contract = contractRepository.findById(contractId);
        if(countOfShares > contract.getCountOfContracts()) return null;

        SalesOffer salesOffer = SalesOffer.builder().contractId(counterService.getNextId("offers")).countOfContracts(countOfShares).valueOfShares(sellPrice).build();
        contract.addOffer(salesOffer);
        contractRepository.update(contract);
        salesOfferRepository.save(salesOffer);


        return contract;
    }

    public Contract deleteOffer(int offerId) {

        Optional<SalesOffer> optSalesOffer = salesOfferRepository.findById(offerId);

        if(!optSalesOffer.isPresent()) return null;
        SalesOffer salesOffer = optSalesOffer.get();
        Contract contract = contractRepository.findById(salesOffer.getContractId());
        contract.deleteOffer(offerId);
        contractRepository.update(contract);
        salesOfferRepository.delete(salesOffer);

        return contract;
    }

    public ContractDetailsResponse getContractDetails(int betId) {
        Optional<Bet> optBet = betRepository.findById(betId);
        if(!optBet.isPresent()) return ContractDetailsResponse.builder().info("Nie znaleziono tego zakładu").build();
        Bet bet = optBet.get();
        Optional<PredictionMarket> optMarket = predictionMarketRepository.findByMarketId(bet.getMarketId());
        if(!optMarket.isPresent()) return ContractDetailsResponse.builder().info("Nie znaleziono tego rynku").build();
        PredictionMarket market = optMarket.get();

        return ContractDetailsResponse.builder().info("Pobrano dane").bet(bet).predictionMarket(market).build();
    }
}
