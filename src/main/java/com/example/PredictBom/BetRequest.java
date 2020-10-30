package com.example.PredictBom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetRequest {

    private int marketId;
    private int yesPrice;
    private int noPrice;
    private String chosenOption;
}
