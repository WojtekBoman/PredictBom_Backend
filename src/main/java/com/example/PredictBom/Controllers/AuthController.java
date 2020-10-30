package com.example.PredictBom.Controllers;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


import com.example.PredictBom.ChangePasswordWithTokenRequest;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Payload.Request.LoginRequest;
import com.example.PredictBom.Payload.Request.SignupRequest;
import com.example.PredictBom.Payload.Response.JwtResponse;
import com.example.PredictBom.Payload.Response.MessageResponse;
import com.example.PredictBom.Repositories.ModeratorRepository;
import com.example.PredictBom.Repositories.PlayerRepository;
import com.example.PredictBom.Repositories.RoleRepository;
import com.example.PredictBom.Repositories.UserRepository;
import com.example.PredictBom.Security.JWT.JwtUtils;
import com.example.PredictBom.Security.Services.UserDetailsImpl;
import com.example.PredictBom.Services.PasswordResetTokenService;
import com.example.PredictBom.Services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;

import static javax.crypto.Cipher.SECRET_KEY;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());



        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getUsername(),
                userDetails.getEmail(), userDetails.getFirstName(), userDetails.getSurname(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);

            Player player = new Player(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()), 0, 0, 0);

            player.setRoles(roles);

            playerRepository.save(player);

//            Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
//                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//            roles.add(modRole);
//
//            Moderator moderator = new Moderator(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
//                    signUpRequest.getEmail(),
//                    encoder.encode(signUpRequest.getPassword()));
//
//            moderator.setRoles(roles);
//            moderatorRepository.save(moderator);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        Moderator moderator = new Moderator(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                                signUpRequest.getEmail(),
                                encoder.encode(signUpRequest.getPassword()));

                        moderator.setRoles(roles);
                        moderatorRepository.save(moderator);


                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);

                        Player player = new Player(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                                signUpRequest.getEmail(),
                                encoder.encode(signUpRequest.getPassword()), 1000, 0, 0);

                        player.setRoles(roles);

                        playerRepository.save(player);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
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

}