package com.mg.pingpong.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;

import com.mg.pingpong.entity.Match;
import com.mg.pingpong.entity.Player;
import com.mg.pingpong.repository.MatchRepository;
import com.mg.pingpong.repository.PlayerRepository;

@Controller
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @GetMapping("/matches")
    public String matches(Model model) {
            List<Match> matchList = matchRepository.findAll();
            List<Player> playerList = playerRepository.findAll();

            model.addAttribute("matches", matchList);
            model.addAttribute("players", playerList);

            return "matches";
        }
        
        @PostMapping("/matches/save")
        @Transactional
        public String updateMatch(@RequestParam("id") Long id, 
                            @RequestParam("score1") int score1, 
                            @RequestParam("score2") int score2) {
        Match match = matchRepository.findById(id).orElseThrow();
        
        // 이미 결과가 입력된 경기라면 승패 수정을 위해 기존 점수 반영을 취소할지 결정해야 합니다.
        // 여기서는 단순하게 점수 입력 시 승패를 1회 기록하는 로직으로 구성합니다.
        if (!match.isFinished()) {
            Player p1 = playerRepository.findByName(match.getPlayer1());
            Player p2 = playerRepository.findByName(match.getPlayer2());

            if (score1 > score2) {
                p1.setWin(p1.getWin() + 1);
                p2.setLose(p2.getLose() + 1);
            } else if (score2 > score1) {
                p2.setWin(p2.getWin() + 1);
                p1.setLose(p1.getLose() + 1);
            }
            playerRepository.save(p1);
            playerRepository.save(p2);
            match.setFinished(true);
        }
        
        match.setScore1(score1);
        match.setScore2(score2);
        matchRepository.save(match);

        return "redirect:/matches";
    }

    @PostMapping("/matches/generate")
    public String generateMatches(@RequestParam(value = "selectedPlayerNames", required = false) List<String> selectedNames) {
        
        if (selectedNames == null || selectedNames.size() < 2) {
            return "redirect:/matches?error=min_two_players";
        }

        matchRepository.deleteAll(); 

        for (int i = 0; i < selectedNames.size(); i++) {
            for (int j = i + 1; j < selectedNames.size(); j++) {
                Match match = new Match();
                match.setPlayer1(selectedNames.get(i));
                match.setPlayer2(selectedNames.get(j));
                match.setScore1(0);
                match.setScore2(0);
                matchRepository.save(match);
            }
        }
        return "redirect:/matches";
    }
}