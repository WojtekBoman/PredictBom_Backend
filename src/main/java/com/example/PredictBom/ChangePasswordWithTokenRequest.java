package com.example.PredictBom;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
