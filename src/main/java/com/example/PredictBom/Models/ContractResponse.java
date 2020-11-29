package com.example.PredictBom.Models;

import com.example.PredictBom.Entities.Contract;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ContractResponse {

    private Contract contract;
    private String info;

}
