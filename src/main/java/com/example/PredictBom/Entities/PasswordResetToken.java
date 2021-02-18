package com.example.PredictBom.Entities;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.util.Date;

@Document("passwordResetTokens")
@Builder
@Getter
public class PasswordResetToken {

    private static final int EXPIRATION = 60 * 24;

    @Id
    private final String id;

    private final String token;

    private final User user;

    private final String expiryDate;
}