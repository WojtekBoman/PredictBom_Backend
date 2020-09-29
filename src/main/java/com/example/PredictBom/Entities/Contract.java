package com.example.PredictBom.Entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public class Contract {

    @Id
    private int id;
    private int betId;


}
