package com.example.PredictBom.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OffersToBuyResponse {
    private String dealer;
    private int id;
    private int contractId;
    private int shares;
    private String createdDate;
    private double price;
}
