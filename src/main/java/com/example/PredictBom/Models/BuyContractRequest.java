package com.example.PredictBom.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyContractRequest {

    private double maxPrice;
    private int marketId;
    private int countOfShares;
    private int betId;
    private boolean contractOption;
}
