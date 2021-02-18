package com.example.PredictBom.Controllers;

import com.example.PredictBom.Services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@CrossOrigin
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/chart")
    public ResponseEntity<?> getTransactions(@RequestParam int betId,
                                             @RequestParam boolean option,
                                             @RequestParam String timeAgo) {
        return ResponseEntity.ok(transactionService.getTransactions(betId,option,timeAgo));
    }



    @GetMapping("/dealer")
    public ResponseEntity<?> getDealerTransaction(Principal principal, @RequestParam int option,@RequestParam String betTitle,@RequestParam String marketTitle, @RequestParam String[] marketCategory, String sortAttribute, String sortDirection, Pageable pageable) {
        try {
            switch (option){
                case 1:
                    return ResponseEntity.ok(transactionService.getDealerTransactionsByOption(principal.getName(),true,betTitle,marketTitle,marketCategory,pageable,sortAttribute,sortDirection));
                case 2:
                    return ResponseEntity.ok(transactionService.getDealerTransactionsByOption(principal.getName(),false,betTitle,marketTitle,marketCategory,pageable,sortAttribute,sortDirection));
                default:
                    return ResponseEntity.ok(transactionService.getDealerTransactions(principal.getName(),betTitle,marketTitle,marketCategory,pageable,sortAttribute,sortDirection));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @GetMapping("/purchaser")
    public ResponseEntity<?> getPurchaserTransaction(Principal principal, @RequestParam int option,@RequestParam String betTitle,@RequestParam String marketTitle, @RequestParam String[] marketCategory, String sortAttribute, String sortDirection, Pageable pageable) {
        try {
            switch (option){
                case 1:
                    return ResponseEntity.ok(transactionService.getPurchaserTransactionsAndOption(principal.getName(),true,betTitle,marketTitle,marketCategory,pageable,sortAttribute,sortDirection));
                case 2:
                    return ResponseEntity.ok(transactionService.getPurchaserTransactionsAndOption(principal.getName(),false,betTitle,marketTitle,marketCategory,pageable,sortAttribute,sortDirection));
                default:
                    return ResponseEntity.ok(transactionService.getPurchaserTransactions(principal.getName(),betTitle,marketTitle,marketCategory,pageable,sortAttribute,sortDirection));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

}
