package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

@Getter
@Setter
@Builder
@Document(collection = "contracts")
public class Contract {

    @Id
    private int id;
    private Bet bet;
    private MarketInfo marketInfo;
    private String playerId;
    private boolean contractOption;
    private int countOfContracts;
    @Builder.Default
    private ContractStatus contractStatus = ContractStatus.PENDING;
    private HashSet<SalesOffer> offers;
    @Builder.Default
    private String modifiedDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date());


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

    public void updateOffer(SalesOffer offer) {
        if(offers != null) {
            this.offers.remove(offer);
            this.offers.add(offer);
        }
    }


}
