package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@Builder
@Document(collection = "transactions")
public class Transaction {

    @Id
    private int id;
    @Builder.Default
    private String transactionDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date());
    private boolean option;
    private int shares;
    private double price;
    private Bet bet;
    private MarketInfo marketInfo;
    private String dealer;
    private String purchaser;
}
