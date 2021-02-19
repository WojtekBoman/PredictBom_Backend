package com.example.PredictBom.Entities;

import com.example.PredictBom.Constants.SettingsParams;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

@Document(collection = "users")
@Getter
@Setter
public class Player extends User {

    private double budget;

    @Builder.Default
    private String lastLoginDate = new SimpleDateFormat(SettingsParams.DATE_FORMAT,SettingsParams.LOCALE_PL).format(new Date());

    public Player(String username, String firstName, String surname, String email, String password, double budget) {
        super(username, firstName, surname, email, password);

        this.budget = budget;

    }



}
