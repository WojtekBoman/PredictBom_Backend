package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Getter
@Setter
@Builder
@Document(collection = "transactions")
public class Transaction {

    @Id
    private int id;
}
