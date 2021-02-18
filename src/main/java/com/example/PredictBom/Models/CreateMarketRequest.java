package com.example.PredictBom.Models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
public class CreateMarketRequest {

    @NotBlank
    @Size(min = 3)
    private String topic;

    @NotBlank
    @Size(max = 50)
    @Email
    private String category;

    @Size(min = 3, max = 40)
    private String endDate;

    @NotBlank
    private String description;
}
