package com.example.PredictBom.Controllers;

import com.example.PredictBom.Entities.Bet;
import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.PredictionMarket;
import com.example.PredictBom.Models.AddOfferRequest;
import com.example.PredictBom.Models.BetPrice;
import com.example.PredictBom.Models.ContractDetailsResponse;
import com.example.PredictBom.Services.ContractService;
import com.example.PredictBom.Services.PredictionMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Action;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    ContractService contractService;

    @Autowired
    PredictionMarketService marketService;

//    @GetMapping
//    public ResponseEntity<?> getFilteredPlayerContracts(Principal principal,Pageable pageable){
//        try {
//
//            List<Contract> contracts = new ArrayList<>(contractService.getPlayerContracts(principal.getName()));
//            System.out.println(contracts.size());
//            return getResponseEntity(pageable, contracts);
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Błąd");
//        }
//    }



    @GetMapping("/filtered")
    public ResponseEntity<?> getFilteredPlayerContracts(Principal principal,@RequestParam String contractStatus, @RequestParam int contractOption,@RequestParam String betTitle,@RequestParam String marketTitle, @RequestParam String[] marketCategory, String sortAttribute, String sortDirection, Pageable pageable){
        try {
            List<Contract> contracts;
            switch (contractOption){
                case 1:
                    contracts = new ArrayList<>(contractService.getPlayerContractsByOption(principal.getName(),contractStatus,true,betTitle, marketTitle, marketCategory,sortAttribute,sortDirection));
                    break;
                case 2:
                    contracts = new ArrayList<>(contractService.getPlayerContractsByOption(principal.getName(),contractStatus,false,betTitle, marketTitle, marketCategory,sortAttribute,sortDirection));
                    break;
                default:
                    contracts = new ArrayList<>(contractService.getFilteredPlayerContracts(principal.getName(),contractStatus,betTitle, marketTitle, marketCategory,sortAttribute,sortDirection));
            }

            return getResponseEntity(pageable, contracts);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }

    }

    private ResponseEntity<?> getResponseEntity(Pageable pageable, List<Contract> predictionMarkets) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), predictionMarkets.size());
        Page<Contract> pages = new PageImpl<Contract>(predictionMarkets.subList(start, end), pageable, predictionMarkets.size());

        return ResponseEntity.ok(pages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContractById(@PathVariable int id) {
        Contract contract = contractService.getContractById(id);
        if (contract == null){
            return ResponseEntity.badRequest().body("Nie znaleziono kontraktu o podanym id");
        }else{
            return ResponseEntity.ok(contract);
        }
    }

    @GetMapping("/lastBetPrice")
    public ResponseEntity<?> getLastBetPrice(@RequestParam int betId, @RequestParam boolean option) {
        BetPrice betPrice = marketService.getLastPrice(betId,option);
        if(betPrice == null) {
            return ResponseEntity.badRequest().body("Wystąpił błąd pobierania danych");
        }else{
            return ResponseEntity.ok(betPrice);
        }
    }

    @GetMapping("/details")
    public ResponseEntity<?> getContractDetailsInfo(@RequestParam int betId) {
        ContractDetailsResponse response = contractService.getContractDetails(betId);
        if(response.getPredictionMarket() == null) {
            return ResponseEntity.badRequest().body(response);
        }else{
            return ResponseEntity.ok(response);
        }
    }

//    @GetMapping("/lastPrice")
//    public ResponseEntity<?> getLastContractPrice(@RequestParam int contractId) {
//
//    }

    @PostMapping("/addOffer")
    public ResponseEntity<?> addOffer(Principal principal, @RequestBody AddOfferRequest addOfferRequest){

        Contract contract = contractService.addOffer(principal.getName(),addOfferRequest);
        System.out.println(contract);
        if(contract == null) {
            return ResponseEntity.badRequest().body("Wystąpił błąd");
        }
        return ResponseEntity.ok(contract);
    }

    @DeleteMapping("/deleteOffer")
    public ResponseEntity<?> addOffer(Principal principal,@RequestParam int offerId) {
        Contract contract = contractService.deleteOffer(principal.getName(),offerId);
        if(contract == null) {
            return ResponseEntity.badRequest().body("Wystąpił błąd");
        }
        return ResponseEntity.ok(contract);
    }
}
