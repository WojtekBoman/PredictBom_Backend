package com.example.PredictBom.Models;

import com.example.PredictBom.Entities.SalesOffer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@Builder
public class OffersToBuyResponse {
    private String dealer;
    private int id;
    private int contractId;
    private int countOfContracts;
    private String createdDate;
    private double valueOfShares;
}
