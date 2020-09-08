package com.example.PredictBom.Entities;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "players")
public class Player extends User {

    private float budget;
    private int points;
    private int rankingPosition;

    public Player(String username, String firstName, String surname, String email, String password, float budget, int points, int rankingPosition) {
        super(username, firstName, surname, email, password);

        this.budget = budget;
        this.points = points;
        this.rankingPosition = rankingPosition;
    }


}
