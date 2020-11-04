package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.PasswordResetToken;
import com.example.PredictBom.Entities.Player;
import com.example.PredictBom.Entities.Role;
import com.example.PredictBom.Entities.User;
import com.example.PredictBom.Payload.Response.JwtResponse;
import com.example.PredictBom.Repositories.PasswordResetTokenRepository;
import com.example.PredictBom.Repositories.UserRepository;
import com.example.PredictBom.Security.JWT.JwtUtils;
import com.example.PredictBom.Security.Services.UserDetailsImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    public static final int STATUS_OK = 200;
    public final static int PASSWORDS_NOT_EQUALS = 1;
    public final static int INVALID_TOKEN = 2;
    public final static int EXPIRED_TOKEN = 3;
    public final static int CORRECT_TOKEN = 4;
    public final static int UPDATED_PASSWORD = 5;
    public final static int INCORRECT_PASSWORD = 6;
    public final static int USER_NOT_FOUND = 7;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordEncoder encoder;


    public Optional<User> getUser(String username) {return  userRepository.findByUsername(username);}

    public void createPasswordResetTokenForUser(User user, String token) {
//      new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        //Getting current date
        Calendar cal = Calendar.getInstance();
        //Displaying current date in the desired format

        //Number of Days to add
        cal.add(Calendar.DAY_OF_YEAR,1);
        //Date after adding the days to the current date
        String newDate = sdf.format(cal.getTime());
        System.out.println(newDate);
        String sha256hex = DigestUtils.sha256Hex(token);

        PasswordResetToken myToken = PasswordResetToken.builder().user(user).token(sha256hex).expiryDate(newDate).build();
        passwordResetTokenRepository.save(myToken);
    }

    public int validatePasswordResetToken(String token) throws ParseException {
        Optional<PasswordResetToken> passToken = passwordResetTokenRepository.findByToken(DigestUtils.sha256Hex(token));
//        passwordResetTokenRepository.deleteAllByExpiryDateBefore(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date()));
        return !passToken.isPresent() ? INVALID_TOKEN
                : isTokenExpired(passToken.get()) ? EXPIRED_TOKEN
                : CORRECT_TOKEN;
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) throws ParseException {
        final Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        return sdf.parse(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date())).after(sdf.parse(passToken.getExpiryDate()));
    }

    public int changePasswordWithToken(String newPassword, String repeatPassword, String token) throws ParseException {

        String hashedToken = DigestUtils.sha256Hex(token);
        Optional<PasswordResetToken> passToken = passwordResetTokenRepository.findByToken(hashedToken);

        if (!passToken.isPresent()) return INVALID_TOKEN;

        if(!newPassword.equals(repeatPassword)) return PASSWORDS_NOT_EQUALS;

        int status = validatePasswordResetToken(token);
        if(status != CORRECT_TOKEN) return status;

        User userToUpdate = passToken.get().getUser();
        userToUpdate.setPassword(encoder.encode(newPassword));
        userRepository.update(userToUpdate);


        return UPDATED_PASSWORD;
    }

    public int editPassword(String username, String oldPassword, String newPassword, String repeatedNewPassword) {

        if (newPassword.equals(repeatedNewPassword)) {
            Optional<User> user = getUser(username);
            if (user.isPresent()) {
                User userObj = user.get();
                if (encoder.matches(oldPassword, userObj.getPassword())) {
                    userObj.setPassword(encoder.encode(newPassword));
                    userRepository.save(userObj);
                    return STATUS_OK;
                } else {
                    return INCORRECT_PASSWORD;
                }
            }
            return USER_NOT_FOUND;
        }

        return PASSWORDS_NOT_EQUALS;
    }
}
