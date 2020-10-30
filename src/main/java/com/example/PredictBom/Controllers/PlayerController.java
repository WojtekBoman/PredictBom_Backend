package com.example.PredictBom.Controllers;

import com.example.PredictBom.Entities.Player;
import com.example.PredictBom.Models.PlayerResponse;
import com.example.PredictBom.Repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@CrossOrigin
@RequestMapping("/player")
public class PlayerController {

    @Autowired
    PlayerRepository playerRepository;

    @GetMapping("/{username}")
    public ResponseEntity<?> getPlayer(@PathVariable String username){
        Player player = playerRepository.findByUsername(username);
        return ResponseEntity.ok(PlayerResponse.builder().budget(player.getBudget()).points(player.getPoints()).build());
    }

}
