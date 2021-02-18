package com.example.PredictBom.Models;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ChangePasswordWithTokenRequest {

    @NotBlank
    private String newPassword;

    @NotBlank
    private String repeatedPassword;

    @NotBlank
    private String token;


}
