package com.example.PredictBom.Controllers;

import com.example.PredictBom.Entities.PredictionMarket;
import com.example.PredictBom.Models.BuyContractResponse;
import com.example.PredictBom.Models.OffersToBuyResponse;
import com.example.PredictBom.Services.ContractService;
import com.example.PredictBom.Services.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.util.List;

@Controller
@CrossOrigin
@RequestMapping("/offers")
public class OfferController {

    @Autowired
    ContractService contractService;

    @Autowired
    OfferService offerService;

    @GetMapping
    public ResponseEntity<?> getOffers(Principal principal, @RequestParam int betId, @RequestParam boolean option, Pageable pageable) {

        List<OffersToBuyResponse> offers = contractService.getOffers(principal.getName(),betId,option);
        return getResponseEntity(pageable,offers);
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyShares(Principal principal, @RequestParam int offerId, @RequestParam int countOfShares) {
        BuyContractResponse response = offerService.buyShares(principal.getName(),offerId,countOfShares);
        if(response.getBoughtContract() == null) {
            return ResponseEntity.badRequest().body(response);
        }else return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> getResponseEntity(Pageable pageable, List<OffersToBuyResponse> offers) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), offers.size());
        System.out.println(pageable.getSort().toString());
        Page<OffersToBuyResponse> pages = new PageImpl<OffersToBuyResponse>(offers.subList(start, end), pageable, offers.size());
        return ResponseEntity.ok(pages);
    }
}
