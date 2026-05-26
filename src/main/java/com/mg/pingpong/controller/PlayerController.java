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
        List<Player> allPlayers = playerRepository.findAllByOrderByNameAsc();

        model.addAttribute("p0_3", allPlayers.stream().filter(p -> p.getGrade() <= 3).toList());
        model.addAttribute("p4",   allPlayers.stream().filter(p -> p.getGrade() == 4).toList());
        model.addAttribute("p5",   allPlayers.stream().filter(p -> p.getGrade() == 5).toList());
        model.addAttribute("p6",   allPlayers.stream().filter(p -> p.getGrade() == 6).toList());
        model.addAttribute("p7",   allPlayers.stream().filter(p -> p.getGrade() == 7).toList());
        model.addAttribute("p8_10", allPlayers.stream().filter(p -> p.getGrade() >= 8).toList());

        return "players";
    }
    
    // ... 나머지 메서드(delete, edit, update, save)는 동일하게 유지 ...
    @GetMapping("/players/delete")
    public String deletePlayer(@RequestParam("id") Long id) {
        playerRepository.deleteById(id);
        return "redirect:/players";
    }
    
    @GetMapping("/players/edit")
    public String editPlayer(@RequestParam("id") Long id, Model model) {
        Player player = playerRepository.findById(id).orElse(null);
        model.addAttribute("player", player);
        return "player-edit";
    }
    
    @PostMapping("/players/update")
    public String updatePlayer(@RequestParam("id") Long id, @RequestParam("name") String name, @RequestParam("grade") int grade) {
        Player player = playerRepository.findById(id).orElse(null);
        player.setName(name);
        player.setGrade(grade);
        playerRepository.save(player);
        return "redirect:/players";
    }
    
    @PostMapping("/players/save")
    public String savePlayer(@RequestParam("name") String name, @RequestParam("grade") int grade) {
        Player player = new Player();
        player.setName(name);
        player.setGrade(grade);
        playerRepository.save(player);
        return "redirect:/players";
    }
}