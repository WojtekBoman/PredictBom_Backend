package com.example.PredictBom.Models;

import com.example.PredictBom.Entities.Bet;
import com.example.PredictBom.Entities.PredictionMarket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class ContractDetailsResponse {
    private String info;
    private PredictionMarket predictionMarket;
    private Bet bet;
}
