package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken,String> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteAllByExpiryDateBefore(String date);
}
