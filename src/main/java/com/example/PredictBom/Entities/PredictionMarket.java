package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import sun.java2d.pipe.SpanShapeRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@Document(collection = "markets")
public class PredictionMarket {

    @Id
    private int marketId;


    private String topic;
    private Set<Bet> bets;
    private int correctBetId;
    private MarketCategory category;
    @Builder.Default
    private String createdDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    private String predictedEndDate;
    private String description;
    private String author;
    @Builder.Default
    private boolean isSolved = false;
    @Builder.Default
    private boolean isPublic = false;
    private Binary marketCover;


    public void solveMarket(int correctBetId) {
        this.correctBetId = correctBetId;
        this.isSolved = true;
    }

    public void addBet(Bet bet){
        if(bets == null){
            this.bets = new HashSet<>();
        }
        bets.add(bet);
    }




}
