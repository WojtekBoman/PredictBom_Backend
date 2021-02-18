package com.example.PredictBom.Controllers;

import com.example.PredictBom.Models.AddOfferRequest;
import com.example.PredictBom.Services.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@CrossOrigin
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/filtered")
    public ResponseEntity<?> getFilteredPlayerContracts(Principal principal,@RequestParam String contractStatus, @RequestParam int contractOption,@RequestParam String betTitle,@RequestParam String marketTitle, @RequestParam String[] marketCategory, String sortAttribute, String sortDirection, Pageable pageable){
        try {
            switch (contractOption){
                case 1:
                    return ResponseEntity.ok(contractService.getPlayerContractsByOption(principal.getName(),contractStatus,true,betTitle, marketTitle, marketCategory,pageable,sortAttribute,sortDirection));
                case 2:
                    return ResponseEntity.ok(contractService.getPlayerContractsByOption(principal.getName(),contractStatus,false,betTitle, marketTitle, marketCategory,pageable,sortAttribute,sortDirection));
                default:
                    return ResponseEntity.ok(contractService.getFilteredPlayerContracts(principal.getName(),contractStatus,betTitle, marketTitle, marketCategory,pageable,sortAttribute,sortDirection));
            }


        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PLAYER')")
    public ResponseEntity<?> getContractById(Principal principal,@PathVariable int id) {
        return contractService.getContractById(id,principal.getName());
    }

    @PostMapping("/addOffer")
    public ResponseEntity<?> addOffer(Principal principal, @RequestBody AddOfferRequest addOfferRequest){
        return contractService.addOfferFromContract(principal.getName(),addOfferRequest);
    }

    @DeleteMapping("/deleteOffer")
    public ResponseEntity<?> deleteOffer(Principal principal,@RequestParam int offerId) {

        return contractService.deleteOfferFromContract(principal.getName(),offerId);
    }
}
