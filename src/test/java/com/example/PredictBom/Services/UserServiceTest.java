package com.example.PredictBom.Services;
import com.example.PredictBom.Entities.User;
import com.example.PredictBom.Repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {

    @MockBean
    UserRepository userRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Test
    public void changePassword() {
        String username = "NewUser";
        String email = "newuser@gmail.com";
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
        User user = new User(username,email,firstName,surname,oldPassword);

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
}
