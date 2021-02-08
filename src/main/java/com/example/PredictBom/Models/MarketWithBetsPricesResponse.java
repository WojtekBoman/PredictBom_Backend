package com.example.PredictBom.Models;

import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.PredictionMarket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MarketWithBetsPricesResponse {
    private String info;
    private PredictionMarket predictionMarket;
    private BetPrice betPrice;
}
