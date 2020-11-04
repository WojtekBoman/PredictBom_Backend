package com.example.PredictBom.Controllers;

import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.PredictionMarket;
import com.example.PredictBom.Models.ContractDetailsResponse;
import com.example.PredictBom.Services.ContractService;
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
    public ResponseEntity<?> getFilteredPlayerContracts(Principal principal,@RequestParam int contractOption,@RequestParam String betTitle,@RequestParam String marketTitle, @RequestParam String[] marketCategory, String sortAttribute, String sortDirection, Pageable pageable){
        try {
            List<Contract> contracts;
            switch (contractOption){
                case 1:
                    contracts = new ArrayList<>(contractService.getPlayerContractsByOption(principal.getName(),true,betTitle, marketTitle, marketCategory,sortAttribute,sortDirection));
                    break;
                case 2:
                    contracts = new ArrayList<>(contractService.getPlayerContractsByOption(principal.getName(),false,betTitle, marketTitle, marketCategory,sortAttribute,sortDirection));
                    break;
                default:
                    contracts = new ArrayList<>(contractService.getFilteredPlayerContracts(principal.getName(),betTitle, marketTitle, marketCategory,sortAttribute,sortDirection));
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
    public ResponseEntity<?> addOffer(Principal principal, int contractId, int countOfShares, int price) {

        return ResponseEntity.ok("Git");
    }

    @DeleteMapping("/deleteOffer")
    public ResponseEntity<?> addOffer(Principal principal, int offerId) {

        return ResponseEntity.ok("Git");
    }
}
