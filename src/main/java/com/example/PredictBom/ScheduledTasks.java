package com.example.PredictBom;

import com.example.PredictBom.Repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "0 1 1 * * ?")
    public void deleteExpiredTokens() {
        passwordResetTokenRepository.deleteAllByExpiryDateBefore(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date()));
    }
}
