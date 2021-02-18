package com.example.PredictBom.Services;

import com.example.PredictBom.Constants.AuthConstants;
import com.example.PredictBom.Constants.SettingsParams;
import com.example.PredictBom.Entities.PasswordResetToken;
import com.example.PredictBom.Entities.User;
import com.example.PredictBom.Models.SignupRequest;
import com.example.PredictBom.Repositories.PasswordResetTokenRepository;
import com.example.PredictBom.Repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthServiceTest {

    @MockBean
    UserRepository userRepository;

    @MockBean
    PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Test
    public void signUp() {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        SignupRequest signupRequest = SignupRequest.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .surname(surname)
                .password(password)
                .build();

        ResponseEntity<?> response = authService.signUp(signupRequest);

        assertEquals(response.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void signUpExistingUsername() {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";


        SignupRequest signupRequest = SignupRequest.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .surname(surname)
                .password(password)
                .build();

        when(userRepository.existsByUsername(username)).thenReturn(true);
        ResponseEntity<?> response = authService.signUp(signupRequest);
        assertEquals(response.getStatusCode(),HttpStatus.BAD_REQUEST);
    }

    @Test
    public void signUpExistingEmail() {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        User user = new User(username,email,firstName,surname,password);
        user.setRoles(null);


        SignupRequest signupRequest = SignupRequest.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .surname(surname)
                .password(password)
                .build();

        when(userRepository.existsByEmail(email)).thenReturn(true);
        ResponseEntity<?> response = authService.signUp(signupRequest);
        assertEquals(response.getStatusCode(),HttpStatus.BAD_REQUEST);
    }

        @Test
    public void changePassword() {
        String username = "TestUser";
        String email = "testuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String oldPassword = "password";
        String newPassword = "newPassword";
        String repeatedNewPassword = "newPassword";
        User user = new User(username,email,firstName,surname,oldPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword,user.getPassword())).thenReturn(true);
        ResponseEntity<?> responseEntity = authService.editPassword(username,oldPassword,newPassword,repeatedNewPassword);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void changePasswordIncorrectOldPassword() {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        String oldPassword = "incorrectPassword";
        String newPassword = "newPassword";
        String repeatedNewPassword = "newPassword";
        User user = new User(username,email,firstName,surname,password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        ResponseEntity<?> responseEntity = authService.editPassword(username,oldPassword,newPassword,repeatedNewPassword);
        assertEquals(responseEntity.getBody(), AuthConstants.INCORRECT_PASSWORD_INFO);
    }

    @Test
    public void changePasswordWithNonMatchesPasswords() {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String oldPassword = "password";
        String newPassword = "newPassword1";
        String repeatedNewPassword = "newPassword2";
        User user = new User(username,email,firstName,surname,oldPassword);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        ResponseEntity<?> responseEntity = authService.editPassword(username,oldPassword,newPassword,repeatedNewPassword);
        assertEquals(responseEntity.getBody(), AuthConstants.DIFFERENT_PASSWORDS_INFO);
    }

    @Test
    public void changePasswordUserNotFound() {
        String username = "NewUser";
        String oldPassword = "password";
        String newPassword = "newPassword";
        String repeatedNewPassword = "newPassword";
        ResponseEntity<?> responseEntity = authService.editPassword(username,oldPassword,newPassword,repeatedNewPassword);
        assertEquals(responseEntity.getBody(), AuthConstants.PASSWORD_USER_NOT_FOUND_INFO);
    }

    @Test
    public void changePasswordWithTokenInvalidPasswords() throws ParseException {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String oldPassword = "password";
        String newPassword = "newPassword1";
        String repeatedNewPassword = "newPassword2";
        User user = new User(username,email,firstName,surname,oldPassword);
        String tokenId = "idToken";
        String tokenText = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).expiryDate(new SimpleDateFormat(SettingsParams.DATE_FORMAT).format(new Date())).build();

        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = authService.changePasswordWithToken(newPassword,repeatedNewPassword,tokenText);
        assertEquals(AuthConstants.PASSWORDS_NOT_EQUALS,status);
    }

    @Test
    public void changePasswordWithExpiredToken() throws ParseException {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String oldPassword = "password";
        String newPassword = "newPassword";
        String repeatedNewPassword = "newPassword";
        User user = new User(username,email,firstName,surname,oldPassword);
        String tokenId = "idToken";
        String tokenText = UUID.randomUUID().toString();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        String date24hAgo = new SimpleDateFormat(SettingsParams.DATE_FORMAT).format(cal.getTime());
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).expiryDate(date24hAgo).build();

        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = authService.changePasswordWithToken(newPassword,repeatedNewPassword,tokenText);
        assertEquals(AuthConstants.EXPIRED_TOKEN,status);
    }

    @Test
    public void changePasswordWithToken() throws ParseException {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String oldPassword = "password";
        String newPassword = "newPassword";
        String repeatedNewPassword = "newPassword";
        User user = new User(username,email,firstName,surname,oldPassword);
        String tokenId = "idToken";
        String tokenText = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).expiryDate(new SimpleDateFormat(SettingsParams.DATE_FORMAT).format(new Date())).build();

        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = authService.changePasswordWithToken(newPassword,repeatedNewPassword,tokenText);
        assertEquals(AuthConstants.UPDATED_PASSWORD,status);
    }

    @Test
    public void validateInvalidToken() throws ParseException {

        String tokenText = UUID.randomUUID().toString();
        int status = authService.validatePasswordResetToken(tokenText);
        assertEquals(AuthConstants.INVALID_TOKEN,status);
    }

    @Test
    public void validateExpiredToken() throws ParseException {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String oldPassword = "password";
        User user = new User(username,email,firstName,surname,oldPassword);
        String tokenId = "idToken";
        String tokenText = UUID.randomUUID().toString();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        String date24hAgo = new SimpleDateFormat(SettingsParams.DATE_FORMAT).format(cal.getTime());
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).expiryDate(date24hAgo).build();
        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = authService.validatePasswordResetToken(tokenText);
        assertEquals(AuthConstants.EXPIRED_TOKEN,status);
    }
}
