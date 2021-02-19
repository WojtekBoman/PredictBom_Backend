package com.example.PredictBom;

import com.example.PredictBom.Constants.SettingsParams;
import com.example.PredictBom.Repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "0 1 1 * * ?")
    public void deleteExpiredTokens() {
        passwordResetTokenRepository.deleteAllByExpiryDateBefore(new SimpleDateFormat(SettingsParams.DATE_FORMAT, SettingsParams.LOCALE_PL).format(new Date()));
    }
}
