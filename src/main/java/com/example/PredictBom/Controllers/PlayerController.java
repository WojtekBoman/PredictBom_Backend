package com.example.PredictBom.Controllers;

import com.example.PredictBom.Entities.Player;
import com.example.PredictBom.Models.PlayerResponse;
import com.example.PredictBom.Repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@CrossOrigin
@RequestMapping("/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerRepository playerRepository;

    @GetMapping("/{username}")
    public ResponseEntity<?> getPlayer(@PathVariable String username){
        Player player = playerRepository.findByUsername(username);
        return ResponseEntity.ok(PlayerResponse.builder().budget(player.getBudget()).build());
    }

    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking() {
        Pageable pageable = PageRequest.of(0,100,Sort.by("budget").descending());
        return ResponseEntity.ok(playerRepository.findByOrderByBudgetDesc(pageable));
    }

}
