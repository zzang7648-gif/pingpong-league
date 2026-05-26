package com.mg.pingpong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mg.pingpong.entity.Player;
import com.mg.pingpong.repository.PlayerRepository;
import java.util.List;
import org.springframework.ui.Model;

@Controller
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping("/players")
    public String players(Model model) {

    	List<Player> playerList = playerRepository.findAllByOrderByWinDesc();

        model.addAttribute("players", playerList);

        return "players";
    }
    
    @GetMapping("/players/delete")
    public String deletePlayer(@RequestParam("id") Long id) {

        playerRepository.deleteById(id);

        return "redirect:/players";
    }
    
    @GetMapping("/players/edit")
    public String editPlayer(
            @RequestParam("id") Long id,
            Model model
    ) {

        Player player = playerRepository.findById(id).orElse(null);

        model.addAttribute("player", player);

        return "player-edit";
    }
    
    @PostMapping("/players/update")
    public String updatePlayer(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("grade") int grade
    ) {

        Player player = playerRepository.findById(id).orElse(null);

        player.setName(name);
        player.setGrade(grade);

        playerRepository.save(player);

        return "redirect:/players";
    }
    
    
    @PostMapping("/players/save")
    public String savePlayer(
            @RequestParam("name") String name,
            @RequestParam("grade") int grade
    ) {

        Player player = new Player();

        player.setName(name);
        player.setGrade(grade);

        playerRepository.save(player);

        return "redirect:/players";
    }
}