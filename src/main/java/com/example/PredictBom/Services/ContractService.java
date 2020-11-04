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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

//    public List<Contract> getPlayerContracts(String username) {
//        List<Contract> contracts = contractRepository.findByPlayerIdOrderByModifiedDateDesc(username);
//        return contracts.stream().filter(contract -> !contract.getPredictionMarket().isSolved()).collect(Collectors.toList());
//    }

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

    public List<Contract> getPlayerContractsByOption(String username,boolean chosenOption,String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection){
        List<Contract> contracts = contractRepository.findByPlayerIdAndContractOption(username,chosenOption,Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));

        return getContracts(betTitle,marketTitle, marketCategory, contracts);
    }

    public List<Contract> getFilteredPlayerContracts(String username, String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {
        System.out.println(sortDirection + " "+sortAttribute);
//        List<Contract> contracts = contractRepository.findByPlayerId(username,Sort.by(Sort.Direction.fromString(sortDirection),"modifiedDate"));
        List<Contract> contracts = contractRepository.findNotSolvedMarketsByPlayerId(username,Sort.by(Sort.Direction.fromString(sortDirection),"modifiedDate"));

        return getContracts(betTitle,marketTitle, marketCategory, contracts);
    }

    private List<Contract> getContracts(String betTitle,String marketTitle, String[] marketCategory, List<Contract> contractsList) {
        List<Contract> marketsFilteredByTitle = contractsList.stream().filter(item -> item.getPredictionMarket().getTopic().toLowerCase().contains(marketTitle.toLowerCase())).collect(Collectors.toList());
        if(marketsFilteredByTitle.size() == 0) return marketsFilteredByTitle;
        List<Contract> contracts = marketsFilteredByTitle.stream().filter(item -> item.getBet().getChosenOption().toLowerCase().contains(betTitle.toLowerCase())).collect(Collectors.toList());
        if(marketCategory.length == 0) return contracts;
        List<Contract> filteredMarkets = new ArrayList<>();
        for(String market : marketCategory) {
            filteredMarkets.addAll(contracts.stream().filter(item -> item.getPredictionMarket().getCategory().toString().toLowerCase().contains(market.toLowerCase())).collect(Collectors.toList()));
        }
        return filteredMarkets;
    }
}
