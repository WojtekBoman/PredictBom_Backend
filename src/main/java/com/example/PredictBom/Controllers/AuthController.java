package com.example.PredictBom.Controllers;

import java.security.Principal;
import java.text.ParseException;

import com.example.PredictBom.Constants.AuthConstants;
import com.example.PredictBom.Models.ChangePasswordWithTokenRequest;
import com.example.PredictBom.Models.EditPasswordRequest;
import com.example.PredictBom.Models.LoginRequest;
import com.example.PredictBom.Models.SignupRequest;
import com.example.PredictBom.Services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        return authService.signIn(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return authService.signUp(signUpRequest);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPasswordEmail(@RequestParam("username") String email) {

        return authService.resetPasswordEmail(email);
    }

    @GetMapping("/checkToken")
    public ResponseEntity<?> checkToken(
            @RequestParam("token") String token) throws ParseException {

        int result = authService.validatePasswordResetToken(token);
        switch (result) {
            case AuthConstants.CORRECT_TOKEN:
                return ResponseEntity.ok("Podano prawidłowy token");
            case AuthConstants.EXPIRED_TOKEN:
                return ResponseEntity.badRequest().body("Token wygasnął");
            case AuthConstants.INVALID_TOKEN:
                return ResponseEntity.badRequest().body("Nieprawidłowy token");
            default:
                return ResponseEntity.badRequest().body("Wystąpił błąd");
        }
    }


    @PostMapping("/changePassword")
    public ResponseEntity<?> changePasswordWithToken(@RequestBody ChangePasswordWithTokenRequest tokenRequest) throws ParseException {
        int status = authService.changePasswordWithToken(tokenRequest.getNewPassword(), tokenRequest.getRepeatedPassword(), tokenRequest.getToken());

        switch (status) {
            case AuthConstants.UPDATED_PASSWORD:
                return ResponseEntity.ok("Zmieniono hasło");
            case AuthConstants.EXPIRED_TOKEN:
                return ResponseEntity.badRequest().body("Token wygasnął");
            case AuthConstants.INVALID_TOKEN:
                return ResponseEntity.badRequest().body("Nieprawidłowy token");
            default:
                return ResponseEntity.badRequest().body("Wystąpił błąd");
        }
    }

    @PutMapping("/editPassword")
    public ResponseEntity<?> editPassword(Principal principal, @RequestBody EditPasswordRequest editPasswordRequest) {
        return authService.editPassword(principal.getName(), editPasswordRequest.getOldPassword(), editPasswordRequest.getNewPassword(), editPasswordRequest.getRepeatedNewPassword());
    }

}