package com.example.PredictBom.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddOfferRequest {
    private int contractId;
    private int shares;
    private double price;
}
