package com.mg.pingpong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

        // 1. 공통된 필터링 로직을 함수처럼 사용하는 방법 (가독성 UP)
        model.addAttribute("p0_3", allPlayers.stream()
                .filter(p -> getGradeInt(p) <= 3)
                .toList());
        model.addAttribute("p4", allPlayers.stream()
                .filter(p -> getGradeInt(p) == 4)
                .toList());
        model.addAttribute("p5", allPlayers.stream()
                .filter(p -> getGradeInt(p) == 5)
                .toList());
        model.addAttribute("p6", allPlayers.stream()
                .filter(p -> getGradeInt(p) == 6)
                .toList());
        model.addAttribute("p7", allPlayers.stream()
                .filter(p -> getGradeInt(p) == 7)
                .toList());
        model.addAttribute("p8_10", allPlayers.stream()
                .filter(p -> getGradeInt(p) >= 8)
                .toList());

        return "players"; // 반드시 템플릿 이름(예: players.html)을 리턴해야 합니다!
    }

    // 2. 등급을 안전하게 숫자로 변환하는 헬퍼 메서드 추가
    private int getGradeInt(Player p) {
        if (p.getGrade() == null || p.getGrade().isEmpty()) return 0;
        String numOnly = p.getGrade().replaceAll("[^0-9]", "");
        return numOnly.isEmpty() ? 0 : Integer.parseInt(numOnly);
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
        player.setGrade(String.valueOf(grade));
        playerRepository.save(player);
        return "redirect:/players";
    }
    
    @PostMapping("/players/save")
    public String savePlayer(@RequestParam("name") String name, @RequestParam("grade") int grade) {
        Player player = new Player();
        player.setName(name);
       
        player.setGrade(String.valueOf(grade));
        playerRepository.save(player);
        return "redirect:/players";
    }

        // PlayerController.java 내부
    @PostMapping("/players/updatePayment")
    @ResponseBody
    public String updatePayment(@RequestParam("id") Long id, @RequestParam("paid") boolean paid) {
        Player player = playerRepository.findById(id).orElseThrow();
        player.setPaidMembership(paid);
        player.setMembershipDate(java.time.LocalDate.now().toString());
        playerRepository.save(player);
        return "success";
    }

    // 기존 players 매핑 아래에 membership 화면용 매핑 추가
    @GetMapping("/players/membership")
    public String membership(Model model) {
        // 90명의 전체 리스트를 이름순으로 가져옴
        model.addAttribute("players", playerRepository.findAllByOrderByNameAsc());
        return "membership"; // membership.html 템플릿을 호출
    }

}