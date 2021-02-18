package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "bets")
public class Bet {

    @Id
    private int id;
    private int marketId;
    private String title;

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o == this) return true;
        if(!(o instanceof Bet)) return false;
        return id == ((Bet) o).getId();
    }

    @Override
    public int hashCode() {
        return ((Integer) id).hashCode();
    }
}

