package com.example.PredictBom.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BetPrice {

    private int betId;
    private double yesPrice;
    private double noPrice;
    private double lastYesPrice;

}
