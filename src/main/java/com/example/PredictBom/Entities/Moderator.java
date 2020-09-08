package com.example.PredictBom.Entities;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "moderators")
public class Moderator extends User {

    private int createdMarkets;

    public Moderator(String username, String firstName, String surname, String email, String password) {
        super(username, firstName, surname, email, password);
        this.createdMarkets = 0;
    }
}
