package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;

import java.util.function.BinaryOperator;

@Getter
@Setter
@Builder
public class MarketInfo {
    private String topic;
    private MarketCategory marketCategory;
    private Binary marketCover;
}
