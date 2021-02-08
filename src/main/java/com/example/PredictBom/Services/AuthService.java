package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.LoginRequest;
import com.example.PredictBom.Models.SignupRequest;
import com.example.PredictBom.Models.JwtResponse;
import com.example.PredictBom.Models.MessageResponse;
import com.example.PredictBom.Repositories.ModeratorRepository;
import com.example.PredictBom.Repositories.PlayerRepository;
import com.example.PredictBom.Repositories.RoleRepository;
import com.example.PredictBom.Repositories.UserRepository;
import com.example.PredictBom.Security.JWT.JwtUtils;
import com.example.PredictBom.Security.Services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    public static final String USERNAME_ALREADY_USED_INFO = "Nazwa użytkownika jest już zajęta";
    public static final String EMAIL_ALREADY_USED_INFO = "Podany adres e-mail jest już zajęty";
    public static final String USER_SUCCESSFUL_REGISTERED = "Użytkownik został poprawnie zarejestrowany";
    public static final String ROLE_IS_NOT_FOUND_INFO = "Nie znaleziono podanej roli użytkownika";

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
    JwtUtils jwtUtils;

    public JwtResponse signIn(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if(roles.get(0).equals("ROLE_PLAYER")) {
            Player player = playerRepository.findByUsername(userDetails.getUsername());
            String[] lastLoginDate = player.getLastLoginDate().split("-");
            String date = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss").format(new Date());
            String[] dateSplit = date.split("-");

            if(!lastLoginDate[0].equals(dateSplit[0])) {
                player.setBudget(player.getBudget() + 100);
            }else if(!lastLoginDate[1].equals(dateSplit[1])){
                player.setBudget(player.getBudget() + 100);
            }else if(!lastLoginDate[2].substring(0,4).equals(dateSplit[2].substring(0,4))) {
                player.setBudget(player.getBudget() + 100);
            }
            player.setLastLoginDate(date);
            playerRepository.update(player);
        }

        return new JwtResponse(jwt,
                userDetails.getUsername(),
                userDetails.getEmail(), userDetails.getFirstName(), userDetails.getSurname(),
                roles);
    }

    public ResponseEntity signUp(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(USERNAME_ALREADY_USED_INFO));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(EMAIL_ALREADY_USED_INFO));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                    .orElseThrow(() -> new RuntimeException(ROLE_IS_NOT_FOUND_INFO));
            roles.add(userRole);

            Player player = new Player(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()), 1000);

            player.setRoles(roles);

            playerRepository.save(player);
            return ResponseEntity.ok(new MessageResponse(ROLE_IS_NOT_FOUND_INFO));
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
                                .orElseThrow(() -> new RuntimeException(ROLE_IS_NOT_FOUND_INFO));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException(ROLE_IS_NOT_FOUND_INFO));
                        roles.add(modRole);

                        Moderator moderator = new Moderator(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                                signUpRequest.getEmail(),
                                encoder.encode(signUpRequest.getPassword()));

                        moderator.setRoles(roles);
                        moderatorRepository.save(moderator);


                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                                .orElseThrow(() -> new RuntimeException(ROLE_IS_NOT_FOUND_INFO));
                        roles.add(userRole);

                        Player player = new Player(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                                signUpRequest.getEmail(),
                                encoder.encode(signUpRequest.getPassword()), 1000);

                        player.setRoles(roles);

                        playerRepository.save(player);

                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse(USER_SUCCESSFUL_REGISTERED));
    }
}
