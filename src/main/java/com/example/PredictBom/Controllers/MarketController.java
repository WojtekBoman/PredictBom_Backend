package com.example.PredictBom.Controllers;

import com.example.PredictBom.CreateMarketRequest;
import com.example.PredictBom.Entities.PredictionMarket;
import com.example.PredictBom.Models.PredictionMarketResponse;
import com.example.PredictBom.Services.PredictionMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@Controller
@CrossOrigin
@RequestMapping("/markets")
public class MarketController {


    @Autowired
    PredictionMarketService predictionMarketService;

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> createPredictionMarket(Principal principal,@RequestBody CreateMarketRequest createMarketRequest) throws IOException {
//        CreatePredictionMarketResponse response = predictionMarketService.createPredictionMarket(principal.getName(),createMarketRequest.getMarketTitle(),createMarketRequest.getMarketCategory(),createMarketRequest.getPredictedDateEnd(),createMarketRequest.getMarketCover());

        PredictionMarketResponse response = predictionMarketService.createPredictionMarket(principal.getName(),createMarketRequest.getMarketTitle(),createMarketRequest.getMarketCategory(),createMarketRequest.getPredictedDateEnd(),createMarketRequest.getDescription());

        if(response.getPredictionMarket() != null) {
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/addBet")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> addBet(@RequestParam int marketId,@RequestParam String chosenOption){

        switch (predictionMarketService.addBet(marketId,chosenOption)){
            case PredictionMarketService.MARKET_NOT_FOUND:
                return ResponseEntity.badRequest().body("Nie znaleziono rynku prognostycznego");
            default:
                return ResponseEntity.ok("Dodano zakład");
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getMarkets(){
        return ResponseEntity.ok(predictionMarketService.getAllPredictionMarkets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMarketById(@PathVariable("id") int marketId) {
        PredictionMarket market = predictionMarketService.getMarketById(marketId);

        if(market != null) {
            return ResponseEntity.ok(market);
        }else{
            return ResponseEntity.badRequest().body("Nie znaleziono rynku o podanym id");
        }
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/waitingForBets")
    public ResponseEntity<?> getMarketsWaitingForBets(Principal principal) {
        return ResponseEntity.ok(predictionMarketService.getPredictionMarketsWhereBetsIsNullByAuthor(principal.getName()));
    }

}
