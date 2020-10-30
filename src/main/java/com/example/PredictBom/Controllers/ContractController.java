package com.example.PredictBom.Controllers;

import com.example.PredictBom.Models.ContractDetailsResponse;
import com.example.PredictBom.Services.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Action;
import java.security.Principal;

@Controller
@CrossOrigin
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    ContractService contractService;

    @GetMapping
    public ResponseEntity<?> getPlayerContracts(Principal principal){
        return ResponseEntity.ok(contractService.getPlayerContracts(principal.getName()));
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

    @PostMapping("/addOffer")
    public ResponseEntity<?> addOffer(Principal principal, int contractId, int countOfShares, int price) {

        return ResponseEntity.ok("Git");
    }

    @DeleteMapping("/deleteOffer")
    public ResponseEntity<?> addOffer(Principal principal, int offerId) {

        return ResponseEntity.ok("Git");
    }
}
