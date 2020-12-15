package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.Bet;
import com.example.PredictBom.Entities.MarketCategory;
import com.example.PredictBom.Entities.MarketInfo;
import com.example.PredictBom.Entities.Transaction;
import com.example.PredictBom.Repositories.TransactionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionServiceTest {

    @MockBean
    TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Test
    public void getTransactions() {
        int betId = 1;
        boolean option = true;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        String date24hAgo = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(cal.getTime());
        List<Transaction> transactions = transactionService.getTransactions(betId,option,date24hAgo);
        assertNotNull(transactions);
    }

    @Test
    public void getDealerTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Sportowy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Gospodarczy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Gospodarczy temat").marketCategory(MarketCategory.ECONOMY).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Popularny zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build())
                        .build()
        );
        String[] categories = new String[0];

        when(transactionRepository.findAllByDealer("User", Sort.by(Sort.Direction.fromString("desc"),"transactionDate"))).thenReturn(transactions);
        List<Transaction> filteredTransactions = transactionService.getDealerTransactions("User","","",categories,"transactionDate","desc");
        assertEquals(transactions.size(),filteredTransactions.size());
    }

    @Test
    public void getDealerTransactionsByOption() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Sportowy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Gospodarczy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Gospodarczy temat").marketCategory(MarketCategory.ECONOMY).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Popularny zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build())
                        .build()
        );
        String[] categories = new String[0];

        when(transactionRepository.findAllByDealerAndOption("User",true, Sort.by(Sort.Direction.fromString("desc"),"transactionDate"))).thenReturn(transactions);
        List<Transaction> filteredTransactions = transactionService.getDealerTransactionsByOption("User",true,"","",categories,"transactionDate","desc");
        assertEquals(transactions.size(),filteredTransactions.size());
    }

    @Test
    public void getFilteredDealerTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Sportowy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Gospodarczy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Gospodarczy temat").marketCategory(MarketCategory.ECONOMY).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Popularny zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build())
                        .build()
        );
        String[] categories = new String[0];

        when(transactionRepository.findAllByDealer("User", Sort.by(Sort.Direction.fromString("desc"),"transactionDate"))).thenReturn(transactions);
        List<Transaction> filteredTransactions = transactionService.getDealerTransactions("User","Sportowy","",categories,"transactionDate","desc");
        assertEquals(1,filteredTransactions.size());
    }

    @Test
    public void getPurchaserTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Sportowy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Gospodarczy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Gospodarczy temat").marketCategory(MarketCategory.ECONOMY).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Popularny zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build())
                        .build()
        );
        String[] categories = new String[0];

        when(transactionRepository.findAllByPurchaser("User", Sort.by(Sort.Direction.fromString("desc"),"transactionDate"))).thenReturn(transactions);
        List<Transaction> filteredTransactions = transactionService.getPurchaserTransactions("User","","",categories,"transactionDate","desc");
        assertEquals(transactions.size(),filteredTransactions.size());
    }

    @Test
    public void getPurchaserTransactionsByOption() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Sportowy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Gospodarczy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Gospodarczy temat").marketCategory(MarketCategory.ECONOMY).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Popularny zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build())
                        .build()
        );
        String[] categories = new String[0];

        when(transactionRepository.findAllByPurchaserAndOption("User",true, Sort.by(Sort.Direction.fromString("desc"),"transactionDate"))).thenReturn(transactions);
        List<Transaction> filteredTransactions = transactionService.getPurchaserTransactionsAndOption("User",true,"","",categories,"transactionDate","desc");
        assertEquals(transactions.size(),filteredTransactions.size());
    }

    @Test
    public void getFilteredPurchaserTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Sportowy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Sportowy temat").marketCategory(MarketCategory.SPORT).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Gospodarczy zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Gospodarczy temat").marketCategory(MarketCategory.ECONOMY).build())
                        .build()
        );
        transactions.add(
                Transaction.builder().dealer("User").price(1).option(true)
                        .bet(Bet.builder().title("Popularny zakład").build())
                        .marketInfo(MarketInfo.builder().topic("Popularny temat").marketCategory(MarketCategory.CELEBRITIES).build())
                        .build()
        );
        String[] categories = new String[0];

        when(transactionRepository.findAllByPurchaser("User", Sort.by(Sort.Direction.fromString("desc"),"transactionDate"))).thenReturn(transactions);
        List<Transaction> filteredTransactions = transactionService.getPurchaserTransactions("User","Sportowy","",categories,"transactionDate","desc");
        assertEquals(1,filteredTransactions.size());
    }


}
