package com.example.PredictBom.Payload.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String firstName;
    private String surname;
    private List<String> roles;

    public JwtResponse(String token, String username, String email, String firstName, String surname, List<String> roles) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.surname = surname;
        this.roles = roles;
    }
}
