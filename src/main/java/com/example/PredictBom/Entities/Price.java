package com.example.PredictBom.Entities;

import lombok.Setter;
import lombok.Getter;

import java.util.Date;

@Getter
@Setter
public class Price {

    private float price;
    private Date date;

    public Price(float price, Date date) {
        this.price = price;
        this.date = date;
    }
}
