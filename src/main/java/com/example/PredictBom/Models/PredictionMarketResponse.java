package com.example.PredictBom.Models;

import com.example.PredictBom.Entities.PredictionMarket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PredictionMarketResponse {

    private String info;
    private PredictionMarket predictionMarket;

    public PredictionMarketResponse(String info, PredictionMarket predictionMarket) {
        this.info = info;
        this.predictionMarket = predictionMarket;
    }
}
