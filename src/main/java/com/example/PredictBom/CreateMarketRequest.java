package com.example.PredictBom;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.multipart.MultipartFile;

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
    private String predictedEndDate = "3000-01-01";

    @NotBlank
    private String description;
}
