package com.example.PredictBom.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlayerResponse {
    private float budget;
    private int points;
    private int rankingPosition;
}
