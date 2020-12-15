package com.example.PredictBom.Controllers;

import java.security.Principal;
import java.text.ParseException;
import java.util.*;


import com.example.PredictBom.Models.ChangePasswordWithTokenRequest;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.EditPasswordRequest;
import com.example.PredictBom.Models.LoginRequest;
import com.example.PredictBom.Models.SignupRequest;
import com.example.PredictBom.Repositories.ModeratorRepository;
import com.example.PredictBom.Repositories.PlayerRepository;
import com.example.PredictBom.Repositories.RoleRepository;
import com.example.PredictBom.Repositories.UserRepository;
import com.example.PredictBom.Security.JWT.JwtUtils;
import com.example.PredictBom.Services.AuthService;
import com.example.PredictBom.Services.PasswordResetTokenService;
import com.example.PredictBom.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.example.PredictBom.Services.UserService.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    ModeratorRepository moderatorRepository;

    @Autowired
    JavaMailSender emailSender;

    @Autowired
    PasswordResetTokenService passwordResetTokenService;

    @Autowired
    JwtUtils jwtUtils;


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws ParseException {

        return ResponseEntity.ok(authService.signIn(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
       return authService.signUp(signUpRequest);
    }

    @PostMapping("/user/resetPassword")
    public ResponseEntity resetPassword(HttpServletRequest request, @RequestParam("username") String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Nie znaleziono żadnego użytkownika o tym adresie e-mail");
        }
        String token = UUID.randomUUID().toString();
        User user = userOpt.get();
        userService.createPasswordResetTokenForUser(user, token);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("predictBom@gmail.com");
        message.setTo(email);
        message.setText(token);
        message.setSubject("Reset hasła - Predict Bom");
        emailSender.send(message);

        return ResponseEntity.ok("Wysłano maila");
    }

    @GetMapping("/user/changePassword")
    public ResponseEntity showChangePasswordPage(
            @RequestParam("token") String token) throws ParseException {

        int result = userService.validatePasswordResetToken(token);
        switch (result) {
            case UserService.CORRECT_TOKEN:
                return ResponseEntity.ok("Podano prawidłowy token");
            case UserService.EXPIRED_TOKEN:
                return ResponseEntity.badRequest().body("Token wygasnął");
            case UserService.INVALID_TOKEN:
                return ResponseEntity.badRequest().body("Nieprawidłowy token");
            default:
                return ResponseEntity.badRequest().body("Wystąpił błąd");
        }
    }


    @PostMapping("/user/changePassword")
    public ResponseEntity changePasswordWithToken(@RequestBody ChangePasswordWithTokenRequest tokenRequest) throws ParseException {
        int status = userService.changePasswordWithToken(tokenRequest.getNewPassword(),tokenRequest.getRepeatedPassword(),tokenRequest.getToken());

        switch (status){
            case UserService.UPDATED_PASSWORD:
                return ResponseEntity.ok("Zmieniono hasło");
            case UserService.EXPIRED_TOKEN:
                return ResponseEntity.badRequest().body("Token wygasnął");
            case UserService.INVALID_TOKEN:
                return ResponseEntity.badRequest().body("Nieprawidłowy token");
            default:
                return ResponseEntity.badRequest().body("Wystąpił błąd");
    }
}

    @PutMapping("/user/editPassword")
    public ResponseEntity editPassword(Principal principal, @RequestBody EditPasswordRequest editPasswordRequest) {
        int changed = userService.editPassword(principal.getName(), editPasswordRequest.getOldPassword(), editPasswordRequest.getNewPassword(), editPasswordRequest.getRepeatedNewPassword());

        switch (changed) {
            case STATUS_OK:
                return ResponseEntity.ok("Hasło zostało zmienione. Zaloguj się przy pomocy nowego hasła !");
            case USER_NOT_FOUND:
                return ResponseEntity.badRequest()
                        .body("Nie udało się zmienić hasła- nie znaleziono użytkownika.");
            case INCORRECT_PASSWORD:
                return ResponseEntity.badRequest()
                        .body("Nie udało się zmienić hasła - niepoprawne hasło.");
            case PASSWORDS_NOT_EQUALS:
                return ResponseEntity.badRequest()
                        .body("Nie udało się zmienić hasła - hasła nie są identyczne.");
            default:
                return ResponseEntity.badRequest()
                        .body("Nie udało się zmienić hasła.");
        }

    }

}