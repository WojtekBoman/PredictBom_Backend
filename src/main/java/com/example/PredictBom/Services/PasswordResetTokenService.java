package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.PasswordResetToken;
import com.example.PredictBom.Repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class PasswordResetTokenService {

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;



}
