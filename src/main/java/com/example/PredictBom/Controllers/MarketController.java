package com.example.PredictBom.Controllers;

import com.example.PredictBom.Models.BetRequest;
import com.example.PredictBom.Models.CreateMarketRequest;
import com.example.PredictBom.Models.BuyContractRequest;
import com.example.PredictBom.Services.PredictionMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;


@Controller
@CrossOrigin
@RequestMapping("/markets")
@RequiredArgsConstructor
public class MarketController {

    private final PredictionMarketService predictionMarketService;

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> createPredictionMarket(Principal principal, @RequestBody CreateMarketRequest createMarketRequest) {

        return predictionMarketService.createPredictionMarket(principal.getName(),createMarketRequest.getTopic(), createMarketRequest.getCategory(), createMarketRequest.getEndDate(), createMarketRequest.getDescription());
    }

    @PostMapping("/addBet")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> addBet(@RequestBody BetRequest betRequest) {

        return predictionMarketService.addBet(betRequest);
    }

    @PostMapping("/deleteBet")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> removeBet(@RequestParam int betId){
        return predictionMarketService.deleteBet(betId);
        }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> deleteMarket(@RequestParam int marketId){
        return predictionMarketService.deleteMarket(marketId);
    }

    @GetMapping("/")
    public ResponseEntity<?> getMarkets(@RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {
        try {
           return predictionMarketService.getPublicMarkets(marketTitle, marketCategory,pageable,sortAttribute,sortDirection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @GetMapping("/solved")
    public ResponseEntity<?> getSolvedMarkets(@RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {
        try {
            return predictionMarketService.getSolvedMarkets(marketTitle, marketCategory,pageable,sortAttribute,sortDirection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/private")
    public ResponseEntity<?> getSolvedModMarkets(Principal principal, @RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {

        try {
            return predictionMarketService.getFilteredPrivateMarkets(principal.getName(), marketTitle, marketCategory,pageable,sortAttribute,sortDirection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/solvedByMod")
    public ResponseEntity<?> getFilteredPrivateMarkets(Principal principal, @RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {

        try {
            return predictionMarketService.getSolvedModMarkets(principal.getName(), marketTitle, marketCategory,pageable,sortAttribute,sortDirection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/public")
    public ResponseEntity<?> getPublicModMarkets(Principal principal, @RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {

        try {
            return predictionMarketService.getPublicModMarkets(principal.getName(), marketTitle, marketCategory,pageable,sortAttribute,sortDirection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMarketById(@PathVariable("id") int marketId) {
        return predictionMarketService.getMarketById(marketId);

    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editMarketById(@PathVariable("id") int marketId, @RequestBody CreateMarketRequest editRequest) {
        return predictionMarketService.editMarket(marketId,editRequest.getTopic(),editRequest.getCategory(),editRequest.getEndDate(),editRequest.getDescription());
    }

    @PostMapping(value = "/marketCover/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> setMarketCover(@PathVariable("id") int marketId, @RequestParam MultipartFile marketCover) throws IOException {

        return predictionMarketService.setMarketCover(marketId, marketCover);

    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/filteredWaitingForBets")
    public ResponseEntity<?> getFilteredMarketsWaitingForBets(Principal principal, @RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {

        try {
            return predictionMarketService.getFilteredMarketsWaitingForBets(principal.getName(), marketTitle, marketCategory,pageable,sortAttribute,sortDirection);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PutMapping("/makePublic")
    public ResponseEntity<?> makeMarketPublic(@RequestParam int marketId) {
        return predictionMarketService.makeMarketPublic(marketId);

    }

    @PostMapping("/buyContract")
    public ResponseEntity<?> buyContract(Principal principal, @RequestBody BuyContractRequest buyContractRequest) {
           return predictionMarketService.buyContract(principal.getName(),buyContractRequest.getBetId(),buyContractRequest.getMarketId(),buyContractRequest.isContractOption(),buyContractRequest.getShares(),buyContractRequest.getMaxPrice());
    }

    @GetMapping("/betPrice")
    public ResponseEntity<?> getLastBetPrice(@RequestParam int betId, @RequestParam Optional<Boolean> option) {
        if(option.isPresent()){
            return ResponseEntity.ok(predictionMarketService.getLastPrice(betId,option.get()));
        }else{
            return ResponseEntity.ok(predictionMarketService.getPrice(betId));
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PutMapping("/solveMarket")
    public ResponseEntity<?> solveSingleBetMarket(@RequestParam int marketId, @RequestParam int betId,@RequestParam boolean correctOption) {
        return predictionMarketService.solveMarket(marketId,betId,correctOption);

    }

}
