package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.ERole;
import com.example.PredictBom.Entities.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}
