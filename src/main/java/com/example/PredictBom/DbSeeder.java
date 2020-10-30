package com.example.PredictBom;

import com.example.PredictBom.Entities.ERole;
import com.example.PredictBom.Entities.Role;
import com.example.PredictBom.Repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbSeeder implements CommandLineRunner {

    private RoleRepository roleRepository;

    public DbSeeder(RoleRepository roleRepository) {this.roleRepository = roleRepository;}

    @Override
    public void run(String... args) throws Exception {
//        Role player = new Role(ERole.ROLE_PLAYER);
//        Role mod = new Role(ERole.ROLE_MODERATOR);
//        Role admin = new Role(ERole.ROLE_ADMIN);
//
//        roleRepository.save(player);
//        roleRepository.save(mod);
//        roleRepository.save(admin);
    }
}
