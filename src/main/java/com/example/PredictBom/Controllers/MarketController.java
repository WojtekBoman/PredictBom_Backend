package com.example.PredictBom.Controllers;

import com.example.PredictBom.BetRequest;
import com.example.PredictBom.CreateMarketRequest;
import com.example.PredictBom.Entities.PredictionMarket;
import com.example.PredictBom.Entities.SalesOffer;
import com.example.PredictBom.Models.BuyContractRequest;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Models.MarketWithBetsPricesResponse;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Services.PredictionMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
@RequestMapping("/markets")
public class MarketController {


    @Autowired
    PredictionMarketService predictionMarketService;

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> createPredictionMarket(Principal principal, @RequestBody CreateMarketRequest createMarketRequest) throws IOException {
//        CreatePredictionMarketResponse response = predictionMarketService.createPredictionMarket(principal.getName(),createMarketRequest.getMarketTitle(),createMarketRequest.getMarketCategory(),createMarketRequest.getPredictedDateEnd(),createMarketRequest.getMarketCover());

        PredictionMarketResponse response = predictionMarketService.createPredictionMarket(principal.getName(),createMarketRequest.getTopic(), createMarketRequest.getCategory(), createMarketRequest.getPredictedEndDate(), createMarketRequest.getDescription());
        System.out.println(response);
        if (response.getPredictionMarket() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

//    @PostMapping("/new")
////    @PreAuthorize("hasRole('ROLE_MODERATOR')")
//    public ResponseEntity<?> createPredictionMarket(Principal principal, @RequestParam String marketTitle, @RequestParam String marketCategory, @RequestParam String predictedDateEnd, @RequestParam String description) throws IOException {
////        CreatePredictionMarketResponse response = predictionMarketService.createPredictionMarket(principal.getName(),createMarketRequest.getMarketTitle(),createMarketRequest.getMarketCategory(),createMarketRequest.getPredictedDateEnd(),createMarketRequest.getMarketCover());
//
//        PredictionMarketResponse response = predictionMarketService.createPredictionMarket(principal.getName(), marketTitle, marketCategory, predictedDateEnd, description);
//        if (response.getPredictionMarket() != null) {
//            return ResponseEntity.ok(response);
//        } else {
//            return ResponseEntity.badRequest().body(response);
//        }
//    }

    @PostMapping("/addBet")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> addBet(@RequestBody BetRequest betRequest) {

        MarketWithBetsPricesResponse response = predictionMarketService.addBet(betRequest);
        if (response.getPredictionMarket() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/deleteBet")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> removeBet(@RequestParam int betId){
        PredictionMarketResponse response = predictionMarketService.deleteBet(betId);
        if (response.getPredictionMarket() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
        }

//    @GetMapping("/")
//    public ResponseEntity<?> getAllMarkets() {
//        return ResponseEntity.ok(predictionMarketService.getAllPredictionMarkets());
//    }

    @GetMapping("/")
    public ResponseEntity<?> getMarkets(@RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {
        try {
            List<PredictionMarket> predictionMarkets = new ArrayList<>(predictionMarketService.getPublicMarkets(marketTitle, marketCategory,sortAttribute,sortDirection));
            return getResponseEntity(pageable, predictionMarkets);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @GetMapping("/solved")
    public ResponseEntity<?> getSolvedMarkets(@RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {
        try {
            List<PredictionMarket> predictionMarkets = new ArrayList<>(predictionMarketService.getSolvedMarkets(marketTitle, marketCategory,sortAttribute,sortDirection));
            return getResponseEntity(pageable, predictionMarkets);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMarketById(@PathVariable("id") int marketId) {
        PredictionMarket market = predictionMarketService.getMarketById(marketId);

        if (market != null) {
            return ResponseEntity.ok(market);
        } else {
            return ResponseEntity.badRequest().body("Nie znaleziono rynku o podanym id");
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editMarketById(@PathVariable("id") int marketId, @RequestBody CreateMarketRequest editRequest) {
        PredictionMarketResponse response = predictionMarketService.editMarket(marketId,editRequest.getTopic(),editRequest.getCategory(),editRequest.getPredictedEndDate(),editRequest.getDescription());

        if(response.getPredictionMarket() != null) {
            return ResponseEntity.ok(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PostMapping(value = "/marketCover/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> setMarketCover(@PathVariable("id") int marketId, @RequestParam MultipartFile marketCover) throws IOException {

        PredictionMarketResponse response = predictionMarketService.setMarketCover(marketId, marketCover);

        if (response.getPredictionMarket() != null) {
            return ResponseEntity.ok(response);
         } else {
             return ResponseEntity.badRequest().body(response);
    }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/waitingForBets")
    public ResponseEntity<?> getMarketsWaitingForBets(Principal principal) {
        return ResponseEntity.ok(predictionMarketService.getPredictionMarketsWhereBetsIsNullByAuthor(principal.getName()));
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/filteredWaitingForBets")
    public ResponseEntity<?> getFilteredMarketsWaitingForBets(Principal principal, @RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {

        try {
            List<PredictionMarket> predictionMarkets = new ArrayList<>(predictionMarketService.getFilteredMarketsWaitingForBets(principal.getName(), marketTitle, marketCategory,sortAttribute,sortDirection));
            return getResponseEntity(pageable, predictionMarkets);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/private")
    public ResponseEntity<?> getFilteredPrivateMarkets(Principal principal, @RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {

        try {
            List<PredictionMarket> predictionMarkets = new ArrayList<>(predictionMarketService.getFilteredPrivateMarkets(principal.getName(), marketTitle, marketCategory,sortAttribute,sortDirection));
            return getResponseEntity(pageable, predictionMarkets);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/public")
    public ResponseEntity<?> getPublicModMarkets(Principal principal, @RequestParam String marketTitle, @RequestParam String[] marketCategory,String sortAttribute,String sortDirection,Pageable pageable) {

        try {
            List<PredictionMarket> predictionMarkets = new ArrayList<>(predictionMarketService.getPublicModMarkets(principal.getName(), marketTitle, marketCategory,sortAttribute,sortDirection));
            return getResponseEntity(pageable, predictionMarkets);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PutMapping("/makePublic")
    public ResponseEntity<?> makeMarketPublic(@RequestParam int marketId) {
        PredictionMarketResponse response = predictionMarketService.makeMarketPublic(marketId);

        if(response.getPredictionMarket() != null) {
            return ResponseEntity.ok(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<?> getResponseEntity(Pageable pageable, List<PredictionMarket> predictionMarkets) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), predictionMarkets.size());
        Page<PredictionMarket> pages = new PageImpl<PredictionMarket>(predictionMarkets.subList(start, end), pageable, predictionMarkets.size());

        return ResponseEntity.ok(pages);
    }

    @PostMapping("/buyContract")
    public ResponseEntity<?> buyContract(Principal principal, @RequestBody BuyContractRequest buyContractRequest) {
            BuyContractResponse response = predictionMarketService.buyContract(principal.getName(),buyContractRequest.getBetId(),buyContractRequest.getMarketId(),buyContractRequest.isContractOption(),buyContractRequest.getCountOfShares(),buyContractRequest.getMaxPrice());
        if(response.getBoughtContract() == null) {
            return ResponseEntity.badRequest().body(response);
        }else{
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/betPrice/{id}")
    public ResponseEntity<?> getBetPrice(@PathVariable int id) {
        return ResponseEntity.ok(predictionMarketService.getPrice(id));
    }

    @PutMapping("/solveMultiBetMarket")
    public ResponseEntity<?> solveMultiBetMarket(@RequestParam int marketId, @RequestParam int betId) {
        PredictionMarketResponse response = predictionMarketService.solveMultiBetMarket(marketId,betId);

        if(response.getPredictionMarket() == null) {
            return ResponseEntity.badRequest().body(response);
        }else{
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/solveSingleBetMarket")
    public ResponseEntity<?> solveSingleBetMarket(@RequestParam int marketId, @RequestParam int betId,@RequestParam boolean correctOption) {
        PredictionMarketResponse response = predictionMarketService.solveSingleBetMarket(marketId,betId,correctOption);

        if(response.getPredictionMarket() == null) {
            return ResponseEntity.badRequest().body(response);
        }else{
            return ResponseEntity.ok(response);
        }
    }
}
