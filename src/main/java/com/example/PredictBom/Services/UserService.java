package com.example.PredictBom.Services;

import com.example.PredictBom.Entities.User;
import com.example.PredictBom.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public Optional<User> getUser(String username) {return  userRepository.findByUsername(username);}


}
