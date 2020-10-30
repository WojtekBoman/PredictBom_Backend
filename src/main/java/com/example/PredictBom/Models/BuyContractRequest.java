package com.example.PredictBom.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyContractRequest {

    private int maxPrice;
    private int countOfShares;
    private int betId;
    private boolean contractOption;
}
