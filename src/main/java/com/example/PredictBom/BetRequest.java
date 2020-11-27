package com.example.PredictBom;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BetRequest {

    private int marketId;
    private double yesPrice;
    private double noPrice;
    private int shares;
    private String chosenOption;
}
