package com.example.PredictBom.Entities;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "admins")
public class Admin extends User {
    public Admin(String username, String firstName, String surname, String email, String password) {
        super(username, firstName, surname, email, password);
    }
}
