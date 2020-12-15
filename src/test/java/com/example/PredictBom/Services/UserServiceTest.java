package com.example.PredictBom.Services;
import com.example.PredictBom.Entities.PasswordResetToken;
import com.example.PredictBom.Entities.User;
import com.example.PredictBom.Repositories.PasswordResetTokenRepository;
import com.example.PredictBom.Repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {

    @MockBean
    UserRepository userRepository;

    @MockBean
    PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

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
        int status = userService.editPassword(username,oldPassword,newPassword,repeatedNewPassword);
        assertEquals(UserService.STATUS_OK,status);
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
        int status = userService.editPassword(username,oldPassword,newPassword,repeatedNewPassword);
        assertEquals(UserService.INCORRECT_PASSWORD,status);
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
        int status = userService.editPassword(username,oldPassword,newPassword,repeatedNewPassword);
        assertEquals(UserService.PASSWORDS_NOT_EQUALS,status);
    }

    @Test
    public void changePasswordUserNotFound() {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String oldPassword = "password";
        String newPassword = "newPassword";
        String repeatedNewPassword = "newPassword";
        User user = new User(username,email,firstName,surname,oldPassword);
        int status = userService.editPassword(username,oldPassword,newPassword,repeatedNewPassword);
        assertEquals(UserService.USER_NOT_FOUND,status);
    }

    @Test
    public void changePasswordWithTokenInvalidToken() throws ParseException {
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
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).build();

//        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = userService.changePasswordWithToken(newPassword,repeatedNewPassword,tokenText);
        assertEquals(UserService.INVALID_TOKEN,status);
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
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).expiryDate(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date())).build();

        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = userService.changePasswordWithToken(newPassword,repeatedNewPassword,tokenText);
        assertEquals(UserService.PASSWORDS_NOT_EQUALS,status);
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

        String date24hAgo = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(cal.getTime());
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).expiryDate(date24hAgo).build();

        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = userService.changePasswordWithToken(newPassword,repeatedNewPassword,tokenText);
        assertEquals(UserService.EXPIRED_TOKEN,status);
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
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).expiryDate(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date())).build();

        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = userService.changePasswordWithToken(newPassword,repeatedNewPassword,tokenText);
        assertEquals(UserService.UPDATED_PASSWORD,status);
    }

    @Test
    public void validateInvalidToken() throws ParseException {

        String tokenText = UUID.randomUUID().toString();
        int status = userService.validatePasswordResetToken(tokenText);
        assertEquals(UserService.INVALID_TOKEN,status);
    }

    @Test
    public void validateExpiredToken() throws ParseException {
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

        String date24hAgo = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(cal.getTime());
        PasswordResetToken token = PasswordResetToken.builder().id(tokenId).token(tokenText).user(user).expiryDate(date24hAgo).build();
        when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));when(passwordResetTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(token));
        int status = userService.validatePasswordResetToken(tokenText);
        assertEquals(UserService.EXPIRED_TOKEN,status);
    }
}
