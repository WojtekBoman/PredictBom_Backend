package com.example.PredictBom.Entities;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

@Builder
@Getter
@Setter
@Document(collection = "offers")
public class SalesOffer implements Comparable<SalesOffer> {

    @Id
    @Indexed(unique = true)
    private int id;
    private int contractId;
    private int countOfContracts;
    @Builder.Default
    private String createdDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date());
    private double valueOfShares;


    @Override
    public boolean equals(Object o) {
        return id == ((SalesOffer) o).getId();
    }

    @Override
    public int hashCode() {
        return ((Integer) id).hashCode();
    }

    @SneakyThrows
    @Override
    public int compareTo(SalesOffer o) {
        if(this.getValueOfShares() > o.getValueOfShares()) return 1;
        if(this.getValueOfShares() < o.getValueOfShares()) return -1;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        return sdf.parse(this.createdDate).compareTo(sdf.parse(o.getCreatedDate()));
    }

//    String x1 = ((Person) o1).getName();
//    String x2 = ((Person) o2).getName();
//    int sComp = x1.compareTo(x2);
//
//            if (sComp != 0) {
//        return sComp;
//    }
//
//    Integer x1 = ((Person) o1).getAge();
//    Integer x2 = ((Person) o2).getAge();
//            return x1.compareTo(x2);
}