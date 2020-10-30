package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@Document(collection = "contracts")
public class Contract {

    @Id
    private int id;
    private int betId;
    private String playerId;
    private boolean contractOption;
    private double valueOfShares;
    private int countOfContracts;
    private HashSet<SalesOffer> offers;


    public void addOffer(SalesOffer offer){
        if(offers == null){
            this.offers = new HashSet<>();
        }
        offers.add(offer);
    }

    public void deleteOffer(int offerId) {
        if(offers != null) {
            SalesOffer offerToDelete = SalesOffer.builder().id(offerId).build();
            this.offers.remove(offerToDelete);
        }
    }


}
