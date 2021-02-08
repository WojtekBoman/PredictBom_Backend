package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.User;
import com.example.PredictBom.Models.SignupRequest;
import com.example.PredictBom.Repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthServiceTest {

    @MockBean
    UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

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

        ResponseEntity response = authService.signUp(signupRequest);

        assertEquals(response.getStatusCodeValue(),200);
    }

    @Test
    public void signUpExistingUsername() {
        String username = "NewUser";
        String email = "newuser@gmail.com";
        String firstName = "John";
        String surname = "Doe";
        String password = "password";
        User user = new User(username,email,firstName,surname,password);


        SignupRequest signupRequest = SignupRequest.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .surname(surname)
                .password(password)
                .build();

        when(userRepository.existsByUsername(username)).thenReturn(true);
        ResponseEntity response = authService.signUp(signupRequest);
        assertEquals(response.getStatusCodeValue(),400);
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
        ResponseEntity response = authService.signUp(signupRequest);
        assertEquals(response.getStatusCodeValue(),400);
    }

}
