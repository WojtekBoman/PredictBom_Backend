package com.example.PredictBom.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

public interface EmailService {

    void sendMessage(String to, String subject, String text);
}
