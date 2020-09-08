package com.example.PredictBom.Entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "markets")
public class PredictionMarket {

    @Id
    private int marketId;

    private String topic;

}
