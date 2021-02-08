package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.AddOfferRequest;
import com.example.PredictBom.Models.ContractResponse;
import com.example.PredictBom.Models.OffersToBuyResponse;
import com.example.PredictBom.Repositories.BetRepository;
import com.example.PredictBom.Repositories.ContractRepository;
import com.example.PredictBom.Repositories.PredictionMarketRepository;
import com.example.PredictBom.Repositories.SalesOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContractService {

    public static final String OFFER_IS_NOT_FOUND_INFO = "Nie znaleziono takiej oferty";
    public static final String CONTRACT_IS_NOT_FOUND_INFO = "Nie znaleziono podanego kontraktu";
    public static final String NOT_ENOUGH_SHARES_INFO = "Nie masz wystarczająco akcji aby dodać tę ofertę";
    public static final String USER_IS_NOT_CONTRACT_OWNER_INFO = "Nie jesteś właścicielem tego kontraktu";
    public static final String ADDED_NEW_OFFER_INFO = "Dodano nową ofertę";
    public static final String DELETED_OFFER_INFO = "Usunięto ofertę";

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
    public ContractResponse addOffer(String username, AddOfferRequest addOfferRequest) {


        Optional<Contract> optContract = contractRepository.findById(addOfferRequest.getContractId());
        if(!optContract.isPresent())
            return ContractResponse.builder().info(CONTRACT_IS_NOT_FOUND_INFO).build();
        Contract contract = optContract.get();
        if(addOfferRequest.getShares() > contract.getShares())
            return ContractResponse.builder().info(NOT_ENOUGH_SHARES_INFO).build();
        if(!contract.getPlayerId().equals(username))
            return ContractResponse.builder().info(USER_IS_NOT_CONTRACT_OWNER_INFO).build();
        Offer salesOffer = Offer.builder()
                .id(counterService.getNextId("offers"))
                .contractId(addOfferRequest.getContractId())
                .shares(addOfferRequest.getShares())
                .price(addOfferRequest.getPrice())
                .build();
        contract.addOffer(salesOffer);
        contract.setShares(contract.getShares() - addOfferRequest.getShares());
        contractRepository.update(contract);
        salesOfferRepository.save(salesOffer);

        return ContractResponse.builder().info(ADDED_NEW_OFFER_INFO).contract(contract).build();
    }

    public List<OffersToBuyResponse> getOffers(int betId, boolean chosenOption) {



        List<Contract> contracts = contractRepository.findOffersToBuy(betId,chosenOption);
        List<OffersToBuyResponse> offers = new ArrayList<>();
        contracts.forEach(contract -> contract.getOffers().forEach(salesOffer -> offers.add(OffersToBuyResponse.builder().dealer(contract.getPlayerId()).contractId(salesOffer.getContractId()).shares(salesOffer.getShares()).price(salesOffer.getPrice()).createdDate(salesOffer.getCreatedDate()).id(salesOffer.getId()).build())));
        Collections.sort(offers, new Comparator<OffersToBuyResponse>() {
            @Override
            public int compare(OffersToBuyResponse o1, OffersToBuyResponse o2) {
                int compared = Double.compare(o1.getPrice(),o2.getPrice());
                return compared == 0 ? o1.getCreatedDate().compareTo(o2.getCreatedDate()) : compared;
            }
        });
        return offers;
    }




    public ContractResponse deleteOffer(String username, int offerId) {

        Optional<Offer> optSalesOffer = salesOfferRepository.findById(offerId);
        if(!optSalesOffer.isPresent()) return ContractResponse.builder().info(OFFER_IS_NOT_FOUND_INFO).build();
        Offer salesOffer = optSalesOffer.get();
        Optional<Contract> optContract = contractRepository.findById(salesOffer.getContractId());
        if(!optContract.isPresent()) return ContractResponse.builder().info(CONTRACT_IS_NOT_FOUND_INFO).build();
        Contract contract = optContract.get();
        if(!contract.getPlayerId().equals(username)) return ContractResponse.builder().info(USER_IS_NOT_CONTRACT_OWNER_INFO).build();
        contract.deleteOffer(offerId);
        contract.setShares(contract.getShares() + salesOffer.getShares());
        contractRepository.update(contract);
        salesOfferRepository.delete(salesOffer);

        return ContractResponse.builder().info(DELETED_OFFER_INFO).contract(contract).build();
    }

    public Contract getContractById(int id, String username) {
        Optional<Contract> contract = contractRepository.findByIdAndPlayerId(id,username);
        return contract.orElse(null);
    }


    public List<Contract> getPlayerContractsByOption(String username,String contractStatus,boolean chosenOption,String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection){
        List<Contract> contracts = contractRepository.findByPlayerIdAndContractOption(username,chosenOption,Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));

        return getContracts(contractStatus,betTitle,marketTitle, marketCategory, contracts);
    }

    public List<Contract> getFilteredPlayerContracts(String username,String contractStatus, String betTitle, String marketTitle, String[] marketCategory, String sortAttribute, String sortDirection) {
//        List<Contract> contracts = contractRepository.findByPlayerId(username,Sort.by(Sort.Direction.fromString(sortDirection),"modifiedDate"));
        List<Contract> contracts = contractRepository.findNotSolvedMarketsByPlayerId(username,Sort.by(Sort.Direction.fromString(sortDirection),"modifiedDate"));

        return getContracts(contractStatus,betTitle,marketTitle, marketCategory, contracts);
    }

    private List<Contract> getContracts(String contractStatus,String betTitle,String marketTitle, String[] marketCategory, List<Contract> contractsList) {
        List<Contract> marketsFilteredByStatus = contractsList.stream().filter(item -> item.getContractStatus().toString().toLowerCase().contains(contractStatus.toLowerCase())).collect(Collectors.toList());
        if(marketsFilteredByStatus.size() == 0) return marketsFilteredByStatus;
        List<Contract> marketsFilteredByTitle = marketsFilteredByStatus.stream().filter(item -> item.getMarketInfo().getTopic().toLowerCase().contains(marketTitle.toLowerCase())).collect(Collectors.toList());
        if(marketsFilteredByTitle.size() == 0) return marketsFilteredByTitle;
        List<Contract> contracts = marketsFilteredByTitle.stream().filter(item -> item.getBet().getTitle().toLowerCase().contains(betTitle.toLowerCase())).collect(Collectors.toList());
        if(marketCategory.length == 0) return contracts;
        List<Contract> filteredMarkets = new ArrayList<>();
        for(String market : marketCategory) {
            filteredMarkets.addAll(contracts.stream().filter(item -> item.getMarketInfo().getMarketCategory().toString().toLowerCase().contains(market.toLowerCase())).collect(Collectors.toList()));
        }
        return filteredMarkets;
    }
}
