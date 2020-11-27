package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.AddOfferRequest;
import com.example.PredictBom.Models.ContractDetailsResponse;
import com.example.PredictBom.Models.OffersToBuyResponse;
import com.example.PredictBom.Repositories.BetRepository;
import com.example.PredictBom.Repositories.ContractRepository;
import com.example.PredictBom.Repositories.PredictionMarketRepository;
import com.example.PredictBom.Repositories.SalesOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collector;
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
    public Contract addOffer(String username, AddOfferRequest addOfferRequest) {

        Optional<Contract> optContract = contractRepository.findById(addOfferRequest.getContractId());
        if(!optContract.isPresent()) return null;
        Contract contract = optContract.get();
        if(addOfferRequest.getCountOfShares() > contract.getCountOfContracts()) return null;
        if(!contract.getPlayerId().equals(username)) return null;
        SalesOffer salesOffer = SalesOffer.builder().id(counterService.getNextId("offers")).contractId(addOfferRequest.getContractId()).countOfContracts(addOfferRequest.getCountOfShares()).valueOfShares(addOfferRequest.getSellPrice()).build();
        contract.addOffer(salesOffer);
        contract.setCountOfContracts(contract.getCountOfContracts() - addOfferRequest.getCountOfShares());
        contractRepository.update(contract);
        salesOfferRepository.save(salesOffer);


        return contract;
    }

    public List<OffersToBuyResponse> getOffers(String username, int betId, boolean chosenOption) {


//        List<SalesOffer> offers = contractRepository.findOffersToBuy(betId,chosenOption,username).stream().map(contract -> new ArrayList<>(contract.getOffers())).collect(Collectors.toList()).stream().flatMap(List::stream)
//                .collect(Collectors.toList());
//
//
        List<Contract> contracts = contractRepository.findOffersToBuy(betId,chosenOption,username);
        List<OffersToBuyResponse> offers = new ArrayList<>();
        contracts.forEach(contract -> contract.getOffers().forEach(salesOffer -> offers.add(OffersToBuyResponse.builder().dealer(contract.getPlayerId()).contractId(salesOffer.getContractId()).countOfContracts(salesOffer.getCountOfContracts()).valueOfShares(salesOffer.getValueOfShares()).createdDate(salesOffer.getCreatedDate()).id(salesOffer.getId()).build())));
        Collections.sort(offers, new Comparator<OffersToBuyResponse>() {
            @Override
            public int compare(OffersToBuyResponse o1, OffersToBuyResponse o2) {
                int compared = Double.compare(o1.getValueOfShares(),o2.getValueOfShares());
                return compared == 0 ? o1.getCreatedDate().compareTo(o2.getCreatedDate()) : compared;
            }
        });
        return offers;
    }




    public Contract deleteOffer(String username,int offerId) {

        Optional<SalesOffer> optSalesOffer = salesOfferRepository.findById(offerId);
        if(!optSalesOffer.isPresent()) return null;
        SalesOffer salesOffer = optSalesOffer.get();
        Optional<Contract> optContract = contractRepository.findById(salesOffer.getContractId());
        if(!optContract.isPresent()) return null;
        Contract contract = optContract.get();
        if(!contract.getPlayerId().equals(username)) return null;
        contract.deleteOffer(offerId);
        contract.setCountOfContracts(contract.getCountOfContracts() + salesOffer.getCountOfContracts());
        if(contract.getOffers().size() == 0) contract.setOffers(null);
        contractRepository.update(contract);
        salesOfferRepository.delete(salesOffer);

        return contract;
    }

    public Contract getContractById(int id) {
        Optional<Contract> contract = contractRepository.findById(id);
        return contract.orElse(null);
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

    public List<Contract> getPlayerContractsByOption(String username,String contractStatus,boolean chosenOption,String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection){
        List<Contract> contracts = contractRepository.findByPlayerIdAndContractOption(username,chosenOption,Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));

        return getContracts(contractStatus,betTitle,marketTitle, marketCategory, contracts);
    }

    public List<Contract> getFilteredPlayerContracts(String username,String contractStatus, String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {
        System.out.println(sortDirection + " "+sortAttribute);
//        List<Contract> contracts = contractRepository.findByPlayerId(username,Sort.by(Sort.Direction.fromString(sortDirection),"modifiedDate"));
        List<Contract> contracts = contractRepository.findNotSolvedMarketsByPlayerId(username,Sort.by(Sort.Direction.fromString(sortDirection),"modifiedDate"));

        return getContracts(contractStatus,betTitle,marketTitle, marketCategory, contracts);
    }

    private List<Contract> getContracts(String contractStatus,String betTitle,String marketTitle, String[] marketCategory, List<Contract> contractsList) {
        List<Contract> marketsFilteredByStatus = contractsList.stream().filter(item -> item.getContractStatus().toString().toLowerCase().contains(contractStatus.toLowerCase())).collect(Collectors.toList());
        if(marketsFilteredByStatus.size() == 0) return marketsFilteredByStatus;
        List<Contract> marketsFilteredByTitle = marketsFilteredByStatus.stream().filter(item -> item.getMarketInfo().getTopic().toLowerCase().contains(marketTitle.toLowerCase())).collect(Collectors.toList());
        if(marketsFilteredByTitle.size() == 0) return marketsFilteredByTitle;
        List<Contract> contracts = marketsFilteredByTitle.stream().filter(item -> item.getBet().getChosenOption().toLowerCase().contains(betTitle.toLowerCase())).collect(Collectors.toList());
        if(marketCategory.length == 0) return contracts;
        List<Contract> filteredMarkets = new ArrayList<>();
        for(String market : marketCategory) {
            filteredMarkets.addAll(contracts.stream().filter(item -> item.getMarketInfo().getMarketCategory().toString().toLowerCase().contains(market.toLowerCase())).collect(Collectors.toList()));
        }
        return filteredMarkets;
    }
}
