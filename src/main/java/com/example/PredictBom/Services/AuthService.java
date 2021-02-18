package com.example.PredictBom.Services;

import com.example.PredictBom.Constants.AuthConstants;
import com.example.PredictBom.Constants.SettingsParams;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Models.LoginRequest;
import com.example.PredictBom.Models.SignupRequest;
import com.example.PredictBom.Models.JwtResponse;
import com.example.PredictBom.Models.MessageResponse;
import com.example.PredictBom.Repositories.*;
import com.example.PredictBom.Security.JWT.JwtUtils;
import com.example.PredictBom.Security.Services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder encoder;

    private final PlayerRepository playerRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final JavaMailSender emailSender;

    private final JwtUtils jwtUtils;

    public ResponseEntity<?> signIn(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (roles.get(0).equals(ERole.ROLE_PLAYER.toString())) {
            updatePlayerBudget(userDetails.getUsername());
        }

        return ResponseEntity.ok(
                JwtResponse.builder().token(jwt).username(userDetails.getUsername()).email(userDetails.getEmail()).firstName(userDetails.getFirstName()).surname(userDetails.getSurname()).roles(roles).build()
        );
    }

    public ResponseEntity<?> signUp(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(AuthConstants.USERNAME_ALREADY_USED_INFO);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(AuthConstants.EMAIL_ALREADY_USED_INFO);
        }

        User user = new User(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();
//
        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                    .orElseThrow(() -> new RuntimeException(AuthConstants.ROLE_IS_NOT_FOUND_INFO));
            roles.add(userRole);

            Player player = new Player(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getSurname(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()), SettingsParams.START_BUDGET);

            player.setRoles(roles);

            playerRepository.save(player);
            return ResponseEntity.ok(AuthConstants.USER_SUCCESSFUL_REGISTERED);

        } else {
            strRoles.forEach(role -> {
                if ("mod".equals(role)) {
                    Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                            .orElseThrow(() -> new RuntimeException(AuthConstants.ROLE_IS_NOT_FOUND_INFO));
                    roles.add(modRole);

                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                            .orElseThrow(() -> new RuntimeException(AuthConstants.ROLE_IS_NOT_FOUND_INFO));
                    roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(AuthConstants.USER_SUCCESSFUL_REGISTERED);
    }

    public ResponseEntity<?> resetPasswordEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(AuthConstants.USER_NOT_FOUND_BY_MAIL_INFO);
        }
        String token = UUID.randomUUID().toString();
        User user = userOpt.get();
        createPasswordResetTokenForUser(user, token);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("predictBom@gmail.com");
        message.setTo(email);
        message.setText(token);
        message.setSubject("Reset hasła - Predict Bom");
        emailSender.send(message);

        return ResponseEntity.ok("Wysłano maila");
    }

    private void createPasswordResetTokenForUser(User user, String token) {

        SimpleDateFormat sdf = new SimpleDateFormat(SettingsParams.DATE_FORMAT);
        //Getting current date
        Calendar cal = Calendar.getInstance();
        //Displaying current date in the desired format

        //Number of Days to add
        cal.add(Calendar.DAY_OF_YEAR, 1);
        //Date after adding the days to the current date
        String newDate = sdf.format(cal.getTime());
        String sha256hex = DigestUtils.sha256Hex(token);

        PasswordResetToken myToken = PasswordResetToken.builder().user(user).token(sha256hex).expiryDate(newDate).build();
        passwordResetTokenRepository.save(myToken);
    }

    public ResponseEntity<?> editPassword(String username, String oldPassword, String newPassword, String repeatedNewPassword) {

        if (newPassword.equals(repeatedNewPassword)) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                User userObj = user.get();
                if (encoder.matches(oldPassword, userObj.getPassword())) {
                    boolean isPlayer = userObj.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_PLAYER);
                    if (isPlayer) {
                        Player player = playerRepository.findByUsername(username);
                        player.setPassword(encoder.encode(newPassword));
                        playerRepository.save(player);
                    } else {
                        userObj.setPassword(encoder.encode(newPassword));
                        userRepository.save(userObj);
                    }
                    return ResponseEntity.ok(AuthConstants.PASSWORD_CHANGED_INFO);
                } else {
                    return ResponseEntity.badRequest()
                            .body(AuthConstants.INCORRECT_PASSWORD_INFO);
                }
            }
            return ResponseEntity.badRequest()
                    .body(AuthConstants.PASSWORD_USER_NOT_FOUND_INFO);
        }

        return ResponseEntity.badRequest()
                .body(AuthConstants.DIFFERENT_PASSWORDS_INFO);
    }

    public int validatePasswordResetToken(String token) throws ParseException {
        Optional<PasswordResetToken> passToken = passwordResetTokenRepository.findByToken(DigestUtils.sha256Hex(token));
        return !passToken.isPresent() ? AuthConstants.INVALID_TOKEN
                : isTokenExpired(passToken.get()) ? AuthConstants.EXPIRED_TOKEN
                : AuthConstants.CORRECT_TOKEN;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(SettingsParams.DATE_FORMAT);
        return sdf.parse(new SimpleDateFormat(SettingsParams.DATE_FORMAT).format(new Date())).after(sdf.parse(passToken.getExpiryDate()));
    }

    public int changePasswordWithToken(String newPassword, String repeatPassword, String token) throws ParseException {

        String hashedToken = DigestUtils.sha256Hex(token);
        Optional<PasswordResetToken> passToken = passwordResetTokenRepository.findByToken(hashedToken);

        if (!passToken.isPresent()) return AuthConstants.INVALID_TOKEN;

        if (!newPassword.equals(repeatPassword)) return AuthConstants.PASSWORDS_NOT_EQUALS;

        int status = validatePasswordResetToken(token);
        if (status != AuthConstants.CORRECT_TOKEN) return status;

        User userToUpdate = passToken.get().getUser();
        userToUpdate.setPassword(encoder.encode(newPassword));
        userRepository.update(userToUpdate);


        return AuthConstants.UPDATED_PASSWORD;
    }

    private void updatePlayerBudget(String username) {
        Player player = playerRepository.findByUsername(username);
        String[] lastLoginDate = player.getLastLoginDate().split("-");
        String date = new SimpleDateFormat(SettingsParams.DATE_FORMAT).format(new Date());
        String[] dateSplit = date.split("-");

        if (!lastLoginDate[0].equals(dateSplit[0]) || !lastLoginDate[1].equals(dateSplit[1]) || !lastLoginDate[2].substring(0, 4).equals(dateSplit[2].substring(0, 4))) {
            player.setBudget(player.getBudget() + SettingsParams.DAILY_BONUS);
        }
        player.setLastLoginDate(date);
        playerRepository.update(player);
    }
}
