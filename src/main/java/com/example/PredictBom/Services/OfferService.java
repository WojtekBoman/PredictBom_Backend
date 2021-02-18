package com.example.PredictBom.Services;

import com.example.PredictBom.Constants.OfferConstants;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Models.OffersToBuyResponse;
import com.example.PredictBom.Repositories.*;
import com.mongodb.MongoCommandException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferService implements BuyingHelper {

    private final ContractRepository contractRepository;

    private final PlayerRepository playerRepository;

    private final PredictionMarketRepository predictionMarketRepository;

    private final CounterService counterService;

    private final SalesOfferRepository salesOfferRepository;

    private final TransactionRepository transactionRepository;


    public ResponseEntity<?> getOffers(int betId, boolean chosenOption, Pageable pageable) {

        List<Contract> contracts = contractRepository.findOffersToBuy(betId,chosenOption);
        List<OffersToBuyResponse> offers = new ArrayList<>();
        contracts.forEach(contract -> contract.getOffers().forEach(salesOffer -> offers.add(OffersToBuyResponse.builder().dealer(contract.getPlayerId()).contractId(salesOffer.getContractId()).shares(salesOffer.getShares()).price(salesOffer.getPrice()).createdDate(salesOffer.getCreatedDate()).id(salesOffer.getId()).build())));
        offers.sort(Comparator.comparingDouble(OffersToBuyResponse::getPrice).thenComparing(OffersToBuyResponse::getCreatedDate));
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), offers.size());
        Page<OffersToBuyResponse> pages = new PageImpl<>(offers.subList(start, end), pageable, offers.size());
        return ResponseEntity.ok(pages);
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    @Retryable( value = MongoCommandException.class,
            maxAttempts = 2, backoff = @Backoff(delay = 100))
    public ResponseEntity<?> buyShares(String username, int offerId, int shares) throws MongoCommandException {

        Player purchaser = playerRepository.findByUsername(username);

        Optional<Offer> optOffer = salesOfferRepository.findById(offerId);
        if (!optOffer.isPresent()) return ResponseEntity.badRequest().body(OfferConstants.OFFER_IS_NOT_FOUND_INFO);

        Offer offer = optOffer.get();
        if (purchaser.getBudget() < shares * offer.getPrice()) return ResponseEntity.badRequest().body(OfferConstants.NOT_ENOUGH_MONEY_INFO);
        if (shares > offer.getShares()) return ResponseEntity.badRequest().body(OfferConstants.NOT_ENOUGH_SHARES_INFO);
        offer.setShares(offer.getShares() - shares);
        Optional<Contract> optContract = contractRepository.findById(offer.getContractId());
        if (!optContract.isPresent()) return ResponseEntity.badRequest().body(OfferConstants.CONTRACT_IS_NOT_FOUND_INFO);
        Contract contract = optContract.get();
        if(contract.getPlayerId() != null && contract.getPlayerId().equals(username)) return ResponseEntity.badRequest().body(OfferConstants.BOUGHT_OWN_OFFERS_INFO);

        ResponseEntity<?> limitInfo = checkBuyingLimit(transactionRepository,username,contract.getBet().getId(), contract.isContractOption(),shares);
        if(limitInfo != null) return limitInfo;

        Player player = playerRepository.findByUsername(contract.getPlayerId());
        if (player != null) {
            player.setBudget(player.getBudget() + shares * offer.getPrice());
            playerRepository.update(player);
        }
        if (offer.getShares() == 0) {
            salesOfferRepository.deleteById(offerId);
            contract.deleteOffer(offerId);
            if (contract.getOffers() == null && contract.getShares() == 0) {
                contractRepository.deleteById(contract.getId());
            } else {
                contractRepository.update(contract);
            }
        } else {
            salesOfferRepository.update(offer);
            contract.updateOffer(offer);
            contractRepository.update(contract);
        }
        Transaction transaction = Transaction.builder()
                .id(counterService.getNextId("transactions"))
                .price(offer.getPrice())
                .dealer(contract.getPlayerId())
                .purchaser(username)
                .bet(contract.getBet())
                .marketInfo(contract.getMarketInfo())
                .option(contract.isContractOption())
                .shares(shares)
                .build();


        transactionRepository.save(transaction);
        purchaser.setBudget(purchaser.getBudget() - shares * offer.getPrice());
        playerRepository.update(purchaser);

        Contract boughtContract = findContractWithSamePrice(contractRepository,predictionMarketRepository,counterService,username, contract.getBet().getMarketId(), contract.getBet().getId(), contract.isContractOption(), shares);
        return ResponseEntity.ok(BuyContractResponse.builder().boughtContract(boughtContract).purchaser(purchaser).build());
    }

}
