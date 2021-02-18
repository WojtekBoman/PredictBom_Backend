package com.example.PredictBom.Services;

import com.example.PredictBom.Constants.ContractConstants;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.AddOfferRequest;
import com.example.PredictBom.Repositories.ContractRepository;
import com.example.PredictBom.Repositories.SalesOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    private final SalesOfferRepository salesOfferRepository;
    
    private final CounterService counterService;


    @Transactional
    public ResponseEntity<?> addOfferFromContract(String username, AddOfferRequest addOfferRequest) {

        Optional<Contract> optContract = contractRepository.findById(addOfferRequest.getContractId());
        if(!optContract.isPresent())
            return ResponseEntity.badRequest().body(ContractConstants.CONTRACT_NOT_FOUND_INFO);
        Contract contract = optContract.get();
        if(addOfferRequest.getShares() > contract.getShares())
            return ResponseEntity.badRequest().body(ContractConstants.NOT_ENOUGH_SHARES_INFO);
        if(!contract.getPlayerId().equals(username))
            return ResponseEntity.badRequest().body(ContractConstants.USER_IS_NOT_CONTRACT_OWNER_INFO);
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

        return ResponseEntity.ok(contract);
    }

    public ResponseEntity<?> deleteOfferFromContract(String username, int offerId) {

        Optional<Offer> optSalesOffer = salesOfferRepository.findById(offerId);
        if(!optSalesOffer.isPresent()) return ResponseEntity.badRequest().body(ContractConstants.OFFER_IS_NOT_FOUND_INFO);
        Offer salesOffer = optSalesOffer.get();
        Optional<Contract> optContract = contractRepository.findById(salesOffer.getContractId());
        if(!optContract.isPresent()) return ResponseEntity.badRequest().body(ContractConstants.CONTRACT_NOT_FOUND_INFO);
        Contract contract = optContract.get();
        if(!contract.getPlayerId().equals(username)) ResponseEntity.badRequest().body(ContractConstants.USER_IS_NOT_CONTRACT_OWNER_INFO);
        contract.deleteOffer(offerId);
        contract.setShares(contract.getShares() + salesOffer.getShares());
        contractRepository.update(contract);
        salesOfferRepository.delete(salesOffer);

        return ResponseEntity.ok(contract);
    }

    public ResponseEntity<?> getContractById(int id, String username) {
        Optional<Contract> contract = contractRepository.findByIdAndPlayerId(id,username);
        if(contract.isPresent()) return ResponseEntity.ok(contract.get());
        return ResponseEntity.badRequest().body(ContractConstants.CONTRACT_NOT_FOUND_INFO);
    }


    public Page<Contract> getPlayerContractsByOption(String username, String contractStatus, boolean chosenOption, String betTitle, String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection){

        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        if(marketCategory.length == 0) {
            return contractRepository.findByPlayerIdAndContractOption(username,contractStatus,chosenOption,betTitle,marketTitle,pageRequest);
        }
        return contractRepository.findByPlayerIdAndContractOption(username,contractStatus,chosenOption,betTitle,marketTitle,Arrays.asList(marketCategory),pageRequest);
    }

    public Page<Contract> getFilteredPlayerContracts(String username,String contractStatus, String betTitle, String marketTitle, String[] marketCategory,Pageable pageable, String sortAttribute, String sortDirection) {

        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),Sort.by(Sort.Direction.fromString(sortDirection),sortAttribute));
        if(marketCategory.length == 0) {
            return contractRepository.findContractsByPlayerId(username,contractStatus,betTitle,marketTitle,pageRequest);
        }
        return contractRepository.findContractsByPlayerId(username,contractStatus,betTitle,marketTitle,Arrays.asList(marketCategory),pageRequest);
    }

}
