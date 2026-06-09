package com.mg.pingpong.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
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

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;

@Controller
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @GetMapping("/matches")
    public String matches(Model model, @RequestParam(value = "date", required = false) String date) {
        String targetDate = (date != null) ? date : java.time.LocalDate.now().toString();

        List<Player> topPlayers = playerRepository.findTop10ActivePlayers();
        List<Player> allPlayers = playerRepository.findAll();
        
        List<Match> matches = matchRepository.findByMatchDate(targetDate);

        java.util.LinkedHashSet<String> participantSet = new java.util.LinkedHashSet<>();
        for (Match m : matches) {
            participantSet.add(m.getPlayer1());
            participantSet.add(m.getPlayer2());
        }
        
        List<String> participantNames = new java.util.ArrayList<>(participantSet);
        
        List<Player> participants = participantNames.stream()
                .map(name -> allPlayers.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();

        List<List<Player>> playerGroups = new ArrayList<>();
        int totalParticipants = participants.size();

        if (totalParticipants >= 10) {
            int half = (int) Math.ceil(totalParticipants / 2.0);
            playerGroups.add(participants.subList(0, half));
            playerGroups.add(participants.subList(half, totalParticipants));
        } else {
            playerGroups.add(participants);
        }

        Map<String, Match> matchMap = new java.util.HashMap<>();
        for (Match m : matches) {
            matchMap.put(m.getPlayer1() + "_" + m.getPlayer2(), m);
            matchMap.put(m.getPlayer2() + "_" + m.getPlayer1(), m);
        }

        model.addAttribute("matchMap", matchMap);
        model.addAttribute("topPlayers", topPlayers);
        model.addAttribute("players", allPlayers);
        model.addAttribute("playerGroups", playerGroups);
        model.addAttribute("matches", matches);
        model.addAttribute("selectedDate", targetDate);

        return "matches";
    }

   @PostMapping("/matches/save")
    @Transactional
    public String updateMatch(@RequestParam("id") Long id,
                            @RequestParam("score1") int score1,
                            @RequestParam("score2") int score2) {
        Match match = matchRepository.findById(id).orElseThrow();

        if (!match.isFinished()) {
            match.setScore1(score1);
            match.setScore2(score2);
            match.setFinished(true);
            updateStats(match); // ✅ margin 포함 통일된 로직 사용
        } else {
            match.setScore1(score1);
            match.setScore2(score2);
        }

        matchRepository.save(match);
        return "redirect:/matches?date=" + match.getMatchDate();
    }

    @PostMapping("/matches/save-all")
    @Transactional
    public String saveAllMatches(@RequestParam("matchIds") List<Long> matchIds,
                                 @RequestParam("scores1") List<Integer> scores1,
                                 @RequestParam("scores2") List<Integer> scores2) {
        String lastDate = "";
        for (int i = 0; i < matchIds.size(); i++) {
            Match match = matchRepository.findById(matchIds.get(i)).orElseThrow();

            // ✅ 이미 완료된 경기는 ELO 재계산 안 함
            if (!match.isFinished()) {
                match.setScore1(scores1.get(i));
                match.setScore2(scores2.get(i));
                match.setFinished(true);
                updateStats(match);
                matchRepository.save(match);
            }

            lastDate = match.getMatchDate();
        }
        return "redirect:/matches?date=" + lastDate;
    }

    @PostMapping("/matches/save-matrix")
    @Transactional
    public String saveMatrix(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Set<Long> matchIds = new HashSet<>();
        for (String key : paramMap.keySet()) {
            if (key.startsWith("s1_") || key.startsWith("s2_")) {
                matchIds.add(Long.parseLong(key.split("_")[1]));
            }
        }

        String matchDate = "";
        for (Long matchId : matchIds) {
            Match match = matchRepository.findById(matchId).orElseThrow();
            matchDate = match.getMatchDate();

            // ✅ 이미 완료된 경기는 ELO 재계산 안 함
            if (!match.isFinished()) {
                String s1Val = request.getParameter("s1_" + matchId);
                String s2Val = request.getParameter("s2_" + matchId);
                match.setScore1(Integer.parseInt(s1Val != null ? s1Val : "0"));
                match.setScore2(Integer.parseInt(s2Val != null ? s2Val : "0"));
                match.setFinished(true);
                updateStats(match);
                matchRepository.save(match);
            }
        }
        return "redirect:/matches?date=" + matchDate;
    }

    private void updateStats(Match match) {
        Player p1 = playerRepository.findByName(match.getPlayer1());
        Player p2 = playerRepository.findByName(match.getPlayer2());
        int s1 = match.getScore1();
        int s2 = match.getScore2();

        // 승패 기록
        if (s1 > s2)      { p1.setWin(p1.getWin() + 1);  p2.setLose(p2.getLose() + 1); }
        else if (s2 > s1) { p2.setWin(p2.getWin() + 1);  p1.setLose(p1.getLose() + 1); }

        // 부수 보정 포함 레이팅
        double r1 = p1.getElo() + getDivisionScore(p1.getGrade()) * 0.5;
        double r2 = p2.getElo() + getDivisionScore(p2.getGrade()) * 0.5;

        // 기대 승률
        double expected1 = 1.0 / (1.0 + Math.pow(10, (r2 - r1) / 400.0));
        double expected2 = 1.0 - expected1;

        // 실제 결과 (margin 반영)
        double actual1, actual2;
        if (s1 > s2) {
            actual1 = calculateMargin(s1, s2); // 1.0 ~ 1.5
            actual2 = 0.0;
        } else if (s2 > s1) {
            actual1 = 0.0;
            actual2 = calculateMargin(s2, s1);
        } else {
            actual1 = 0.5; // 무승부
            actual2 = 0.5;
        }

        double k = 32.0;
        int delta1 = (int)(k * (actual1 - expected1));
        int delta2 = (int)(k * (actual2 - expected2));

        // ✅ 최소 변화량 보장 (승자 +2, 패자 -2)
        if (s1 > s2) {
            delta1 = Math.max(2, delta1);
            delta2 = Math.min(-2, delta2);
        } else if (s2 > s1) {
            delta2 = Math.max(2, delta2);
            delta1 = Math.min(-2, delta1);
        }

        p1.setElo(p1.getElo() + delta1);
        p2.setElo(p2.getElo() + delta2);

        playerRepository.save(p1);
        playerRepository.save(p2);
    }

    @PostMapping("/matches/generate")
    @Transactional
    public String generateMatches(@RequestParam("matchDate") String matchDate,
                                  @RequestParam("selectedPlayerNames") List<String> selectedNames) {
        matchRepository.deleteByMatchDate(matchDate);

        List<String> shuffledNames = new ArrayList<>(selectedNames);
        java.util.Collections.shuffle(shuffledNames);

        for (int i = 0; i < shuffledNames.size(); i++) {
            for (int j = i + 1; j < shuffledNames.size(); j++) {
                Match match = new Match();
                match.setPlayer1(shuffledNames.get(i));
                match.setPlayer2(shuffledNames.get(j));
                match.setMatchDate(matchDate);
                matchRepository.save(match);
            }
        }
        return "redirect:/matches?date=" + matchDate;
    }

    // ✅ recalculate-all: 전체 초기화 후 순서대로 재계산
    @GetMapping("/matches/recalculate-all")
    @Transactional
    public String recalculateAll(@RequestParam("date") String date) {
        List<Match> matches = matchRepository.findByMatchDate(date);

        // 해당 날짜 참가자 ELO/승패 초기화
        Set<String> playerNames = new HashSet<>();
        for (Match m : matches) {
            playerNames.add(m.getPlayer1());
            playerNames.add(m.getPlayer2());
        }
        for (String name : playerNames) {
            Player p = playerRepository.findByName(name);
            if (p != null) {
                p.setWin(0);
                p.setLose(0);
                playerRepository.save(p);
            }
        }

        // ✅ finished된 경기만 순서대로 재계산
        for (Match match : matches) {
            if (match.isFinished()) {
                updateStats(match);
            }
        }

        return "redirect:/matches?date=" + date;
    }

    private int getDivisionScore(String division) {
        if (division == null) return 500;
        return switch (division) {
            case "1부" -> 2000; case "2부" -> 1800; case "3부" -> 1600; case "4부" -> 1400;
            case "5부" -> 1200; case "6부" -> 1000; case "7부" -> 800; case "8부" -> 600;
            default -> 500;
        };
    }

    private double calculateMargin(int s1, int s2) {
        int diff = s1 - s2;
        if (diff >= 3) return 1.5;   // 3:0 완승
        if (diff == 2) return 1.2;   // 3:1
        return 1.0;                  // 3:2 접전
    }
}