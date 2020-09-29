package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@Document(collection = "bets")
public class Bet {

    @Id
    private int id;

    private String chosenOption;
    private List<Price> historyOfPricesYesContracts;
    private List<Price> historyOfPricesNoContracts;

    public void addPriceYesContract(Price price) {
        if(historyOfPricesYesContracts == null) {
            this.historyOfPricesYesContracts = new ArrayList<Price>();
        }
        historyOfPricesYesContracts.add(price);
    }

    public void addPriceNoContract(Price price) {
        if(historyOfPricesNoContracts == null) {
            this.historyOfPricesNoContracts = new ArrayList<Price>();
        }
        historyOfPricesNoContracts.add(price);
    }

}
