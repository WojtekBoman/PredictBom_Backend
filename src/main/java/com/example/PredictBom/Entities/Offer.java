package com.example.PredictBom.Entities;


import com.example.PredictBom.Constants.SettingsParams;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

@Builder
@Getter
@Setter
@Document(collection = "offers")
public class Offer implements Comparable<Offer> {

    @Id
    @Indexed(unique = true)
    private int id;
    private int contractId;
    private int shares;
    @Builder.Default
    private String createdDate = new SimpleDateFormat(SettingsParams.DATE_FORMAT).format(new Date());
    private double price;


    @Override
    public boolean equals(Object o) {
        return id == ((Offer) o).getId();
    }

    @Override
    public int hashCode() {
        return ((Integer) id).hashCode();
    }

    @SneakyThrows
    @Override
    public int compareTo(Offer o) {
        if(this.getPrice() > o.getPrice()) return 1;
        if(this.getPrice() < o.getPrice()) return -1;
        SimpleDateFormat sdf = new SimpleDateFormat(SettingsParams.DATE_FORMAT);
        return sdf.parse(this.createdDate).compareTo(sdf.parse(o.getCreatedDate()));
    }

}