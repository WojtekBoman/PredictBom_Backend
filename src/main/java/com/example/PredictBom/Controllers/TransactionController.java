package com.example.PredictBom.Controllers;

import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.Transaction;
import com.example.PredictBom.Services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @GetMapping("/chart")
    public ResponseEntity<?> getTransactions(@RequestParam int betId,
                                             @RequestParam boolean option,
                                             @RequestParam String timeAgo) {
        return ResponseEntity.ok(transactionService.getTransactions(betId,option,timeAgo));
    }



    @GetMapping("/dealer")
    public ResponseEntity<?> getDealerTransaction(Principal principal, @RequestParam int option,@RequestParam String betTitle,@RequestParam String marketTitle, @RequestParam String[] marketCategory, String sortAttribute, String sortDirection, Pageable pageable) {
        try {
            List<Transaction> transactions;
            System.out.println("Kategorie" + marketCategory.length);
            switch (option){
                case 1:
                    transactions = new ArrayList<>(transactionService.getDealerTransactionsByOption(principal.getName(),true,betTitle,marketTitle,marketCategory,sortAttribute,sortDirection));
                    break;
                case 2:
                    transactions = new ArrayList<>(transactionService.getDealerTransactionsByOption(principal.getName(),false,betTitle,marketTitle,marketCategory,sortAttribute,sortDirection));
                    break;
                default:
                    System.out.println("Dzien dobry");
                    transactions = new ArrayList<>(transactionService.getDealerTransactions(principal.getName(),betTitle,marketTitle,marketCategory,sortAttribute,sortDirection));
            }

            return getResponseEntity(pageable, transactions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    @GetMapping("/purchaser")
    public ResponseEntity<?> getPurchaserTransaction(Principal principal, @RequestParam int option,@RequestParam String betTitle,@RequestParam String marketTitle, @RequestParam String[] marketCategory, String sortAttribute, String sortDirection, Pageable pageable) {
        try {
            List<Transaction> transactions;
            switch (option){
                case 1:
                    transactions = new ArrayList<>(transactionService.getPurchaserTransactionsAndOption(principal.getName(),true,betTitle,marketTitle,marketCategory,sortAttribute,sortDirection));
                    break;
                case 2:
                    transactions = new ArrayList<>(transactionService.getPurchaserTransactionsAndOption(principal.getName(),false,betTitle,marketTitle,marketCategory,sortAttribute,sortDirection));
                    break;
                default:
                    transactions = new ArrayList<>(transactionService.getPurchaserTransactions(principal.getName(),betTitle,marketTitle,marketCategory,sortAttribute,sortDirection));
            }

            return getResponseEntity(pageable, transactions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd");
        }
    }

    private ResponseEntity<?> getResponseEntity(Pageable pageable, List<Transaction> transactions) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), transactions.size());
        Page<Transaction> pages = new PageImpl<Transaction>(transactions.subList(start, end), pageable, transactions.size());

        return ResponseEntity.ok(pages);
    }
}
