package com.example.PredictBom.Models;

import com.example.PredictBom.Constants.SettingsParams;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class JwtResponse {

    private String token;
    @Builder.Default
    private String type = SettingsParams.TOKEN_TYPE;
    private String username;
    private String email;
    private String firstName;
    private String surname;
    private List<String> roles;
    private double budget;

}
