package com.example.PredictBom.Controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    ModeratorRepository moderatorRepository;

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
                userDetails.getEmail(),userDetails.getFirstName(),userDetails.getSurname(),
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
        User user = new User(signUpRequest.getUsername(),signUpRequest.getFirstName(),signUpRequest.getSurname(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);

            Player player = new Player(signUpRequest.getUsername(),signUpRequest.getFirstName(),signUpRequest.getSurname(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()),0,0,0);

            player.setRoles(roles);

            playerRepository.save(player);
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

                        Moderator moderator = new Moderator(signUpRequest.getUsername(),signUpRequest.getFirstName(),signUpRequest.getSurname(),
                                signUpRequest.getEmail(),
                                encoder.encode(signUpRequest.getPassword()));

                        moderator.setRoles(roles);
                        moderatorRepository.save(moderator);


                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);

                        Player player = new Player(signUpRequest.getUsername(),signUpRequest.getFirstName(),signUpRequest.getSurname(),
                                signUpRequest.getEmail(),
                                encoder.encode(signUpRequest.getPassword()),0,0,0);

                        player.setRoles(roles);

                        playerRepository.save(player);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
