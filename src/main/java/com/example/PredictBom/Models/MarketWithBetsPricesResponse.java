package com.example.PredictBom.Models;

import com.example.PredictBom.Entities.PredictionMarket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class MarketWithBetsPricesResponse {
    private PredictionMarket predictionMarket;
    private BetPrice betPrice;
}
