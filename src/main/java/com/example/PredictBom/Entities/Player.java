package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

@Document(collection = "players")
@Getter
@Setter
public class Player extends User {

    private float budget;
    private int points;
    private int rankingPosition;
    @Builder.Default
    private String lastLoginDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date());

    public Player(String username, String firstName, String surname, String email, String password, float budget, int points, int rankingPosition) {
        super(username, firstName, surname, email, password);

        this.budget = budget;
        this.points = points;
        this.rankingPosition = rankingPosition;
    }


}
