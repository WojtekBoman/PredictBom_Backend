package com.example.PredictBom.Controllers;

import com.example.PredictBom.Services.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;

@Controller
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public ResponseEntity<?> getOffers(@RequestParam int betId, @RequestParam boolean option, Pageable pageable) {
        return offerService.getOffers(betId,option,pageable);
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyShares(Principal principal, @RequestParam int offerId, @RequestParam int shares) {
        return offerService.buyShares(principal.getName(),offerId,shares);
    }

}
