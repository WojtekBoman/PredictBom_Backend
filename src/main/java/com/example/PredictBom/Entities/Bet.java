package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Builder
@Document(collection = "bets")
public class Bet {

    @Id
    private int id;
    private int marketId;
    private String chosenOption;

//
//    public void addPriceYesContract(Price price) {
//        if(historyOfPricesYesContracts == null) {
//            this.historyOfPricesYesContracts = new ArrayList<Price>();
//        }
//        historyOfPricesYesContracts.add(price);
//    }
//
//    public void addPriceNoContract(Price price) {
//        if(historyOfPricesNoContracts == null) {
//            this.historyOfPricesNoContracts = new ArrayList<Price>();
//        }
//        historyOfPricesNoContracts.add(price);
//    }

    @Override
    public boolean equals(Object o) {
        return id == ((Bet) o).getId();
    }

    @Override
    public int hashCode() {
        return ((Integer) id).hashCode();
    }
}
