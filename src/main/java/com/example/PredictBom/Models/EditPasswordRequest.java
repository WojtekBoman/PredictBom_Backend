package com.example.PredictBom.Models;

import lombok.Getter;

@Getter
public class EditPasswordRequest {

    private String oldPassword;
    private String newPassword;
    private String repeatedNewPassword;
}
