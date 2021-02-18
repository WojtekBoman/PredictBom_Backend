package com.example.PredictBom.Entities;

import com.example.PredictBom.Constants.SettingsParams;
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
    private int shares;
    @Builder.Default
    private ContractStatus contractStatus = ContractStatus.PENDING;
    private HashSet<Offer> offers;
    @Builder.Default
    private String modifiedDate = new SimpleDateFormat(SettingsParams.DATE_FORMAT).format(new Date());


    public void addOffer(Offer offer){
        if(offers == null){
            this.offers = new HashSet<>();
        }
        offers.add(offer);
    }

    public void deleteOffer(int offerId) {
        if(offers != null) {
            Offer offerToDelete = Offer.builder().id(offerId).build();
            this.offers.remove(offerToDelete);
            if(this.offers.size() == 0)this.offers = null;
        }
    }

    public void updateOffer(Offer offer) {
        deleteOffer(offer.getId());
        addOffer(offer);
    }


}
