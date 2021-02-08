package com.example.PredictBom.Entities;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@Document(collection = "users")
public class User {

    @Id
    private String username;

    private String firstName, surname, email, password;

    @DBRef
    private Set<Role> roles = new HashSet<>();

    public User(String username, String firstName, String surname, String email, String password) {
        this.username = username;
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
        this.password = password;
    }

}
