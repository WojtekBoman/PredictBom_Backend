package com.example.PredictBom.Models;

import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.Player;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyContractResponse {
    private Player purchaser;
    private Contract boughtContract;
}
