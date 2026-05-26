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
        List<Player> players = playerRepository.findAll();
        List<Match> matches = matchRepository.findAll();

        int size = players.size();
        String[][] scoreTable = new String[size][size];

        for (Match m : matches) {
            int i = findIndex(players, m.getPlayer1());
            int j = findIndex(players, m.getPlayer2());
            
            if (i != -1 && j != -1) {
                scoreTable[i][j] = m.getScore1() + ":" + m.getScore2();
                scoreTable[j][i] = m.getScore2() + ":" + m.getScore1();
            }
        }
        model.addAttribute("players", players);
        model.addAttribute("scoreTable", scoreTable);
        return "matches";
    }

    private int findIndex(List<Player> players, String name) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    @PostMapping("/matches/save")
    @Transactional
    public String updateMatch(@RequestParam("id") Long id, 
                            @RequestParam("score1") int score1, 
                            @RequestParam("score2") int score2) {
        Match match = matchRepository.findById(id).orElseThrow();
        
        if (!match.isFinished()) {
            Player p1 = playerRepository.findByName(match.getPlayer1());
            Player p2 = playerRepository.findByName(match.getPlayer2());

            // 1. 승패 반영
            if (score1 > score2) {
                p1.setWin(p1.getWin() + 1);
                p2.setLose(p2.getLose() + 1);
            } else if (score2 > score1) {
                p2.setWin(p2.getWin() + 1);
                p1.setLose(p1.getLose() + 1);
            }

            // 2. Elo 계산 (K-factor 32 적용)
            double k = 32.0;
            double expected1 = 1.0 / (1.0 + Math.pow(10, (p2.getElo() - p1.getElo()) / 400.0));
            double expected2 = 1.0 / (1.0 + Math.pow(10, (p1.getElo() - p2.getElo()) / 400.0));
            
            p1.setElo(p1.getElo() + (int)(k * ((score1 > score2 ? 1.0 : 0.0) - expected1)));
            p2.setElo(p2.getElo() + (int)(k * ((score2 > score1 ? 1.0 : 0.0) - expected2)));

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