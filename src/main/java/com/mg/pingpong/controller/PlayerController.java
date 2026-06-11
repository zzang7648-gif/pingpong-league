package com.mg.pingpong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator; 

import com.mg.pingpong.entity.Player;
import com.mg.pingpong.repository.PlayerRepository;
import java.util.List;
import org.springframework.ui.Model;

import java.util.Map;       
import java.util.HashMap;   
import com.mg.pingpong.service.EloCalculationService;
import jakarta.servlet.http.HttpSession;

@Controller
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private EloCalculationService eloCalculationService;

    @GetMapping("/players")
    public String players(Model model, HttpSession session)  {
        List<Player> allPlayers = playerRepository.findAllByOrderByNameAsc();

        // ✅ matches.html과 동일한 방식으로 TOP 10 계산
        Map<String, Integer> eloMap = eloCalculationService.calculateAllElo();
        Map<String, int[]> winLoseMap = eloCalculationService.calculateWinLose();

         // ✅ TOP 10: ELO 기준 정렬 (실시간 계산된 값)
        List<Map<String, Object>> topPlayers = allPlayers.stream()
            .filter(p -> winLoseMap.containsKey(p.getName()))
            .map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", p.getName());
                m.put("elo", eloMap.getOrDefault(p.getName(), 1500));
                int[] wl = winLoseMap.getOrDefault(p.getName(), new int[]{0, 0});
                m.put("win", wl[0]);
                m.put("lose", wl[1]);
                m.put("grade", p.getGrade());
                return m;
            })
            .sorted((a, b) -> (int) b.get("elo") - (int) a.get("elo"))
            .limit(10)
            .toList();

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

        model.addAttribute("topPlayers", topPlayers);
        // 관리자 여부만 전달
        model.addAttribute("isAdmin", session.getAttribute("isAdmin") != null);
        
      
        return "players";
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
        player.setElo(1500);
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
        List<Player> players = playerRepository.findAllByOrderByNameAsc();

        long paidCount = players.stream().filter(Player::isPaidMembership).count();
        long unpaidCount = players.size() - paidCount;

        model.addAttribute("players", players);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("totalCount", players.size());

        return "membership";
    }

}