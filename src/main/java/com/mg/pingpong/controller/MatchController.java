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
        
        // 1. DB에서 저장된 순서대로 매치를 가져옵니다
        List<Match> matches = matchRepository.findByMatchDate(targetDate);

        // 2. 매치 데이터에서 참가자들을 '나온 순서대로' 뽑아냅니다.
        java.util.LinkedHashSet<String> participantSet = new java.util.LinkedHashSet<>();
        for (Match m : matches) {
            participantSet.add(m.getPlayer1());
            participantSet.add(m.getPlayer2());
        }
        
        // [중요] Set을 List로 변환해서 participantNames 변수에 담아야 합니다!
        List<String> participantNames = new java.util.ArrayList<>(participantSet);
        
        // 3. 이름 리스트를 다시 Player 객체 리스트로 매핑
        List<Player> participants = participantNames.stream()
                .map(name -> allPlayers.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();

        // 4. 이제 셔플 없이 그대로 그룹을 나눕니다.
        List<List<Player>> playerGroups = new ArrayList<>();
        int totalParticipants = participants.size();

        if (totalParticipants >= 10) {
            int half = (int) Math.ceil(totalParticipants / 2.0);
            playerGroups.add(participants.subList(0, half));       // 1조
            playerGroups.add(participants.subList(half, totalParticipants)); // 2조
        } else {
            playerGroups.add(participants);
        }


        // 5. [추가] 화면 행렬에서 점수를 찾기 쉽도록 Map을 만듭니다.
        Map<String, Match> matchMap = new java.util.HashMap<>();
        for (Match m : matches) {
            // player1_player2 라는 키로 매치 정보를 저장
            matchMap.put(m.getPlayer1() + "_" + m.getPlayer2(), m);
            // player2_player1 로도 검색 가능하게 양방향 저장
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
            Player p1 = playerRepository.findByName(match.getPlayer1());
            Player p2 = playerRepository.findByName(match.getPlayer2());

            if (score1 > score2) { p1.setWin(p1.getWin() + 1); p2.setLose(p2.getLose() + 1); }
            else if (score2 > score1) { p2.setWin(p2.getWin() + 1); p1.setLose(p1.getLose() + 1); }

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
            match.setScore1(scores1.get(i));
            match.setScore2(scores2.get(i));
            match.setFinished(true);
            updateStats(match);
            matchRepository.save(match);
            lastDate = match.getMatchDate();
        }
        return "redirect:/matches?date=" + lastDate;
    }

    private void updateStats(Match match) {
        Player p1 = playerRepository.findByName(match.getPlayer1());
        Player p2 = playerRepository.findByName(match.getPlayer2());
        int s1 = match.getScore1();
        int s2 = match.getScore2();

        if (s1 > s2) { p1.setWin(p1.getWin() + 1); p2.setLose(p2.getLose() + 1); }
        else if (s2 > s1) { p2.setWin(p2.getWin() + 1); p1.setLose(p1.getLose() + 1); }

        double p1Rating = p1.getElo() + (getDivisionScore(p1.getGrade()) * 0.5);
        double p2Rating = p2.getElo() + (getDivisionScore(p2.getGrade()) * 0.5);

        double k = 32.0;
        double expected1 = 1.0 / (1.0 + Math.pow(10, (p2Rating - p1Rating) / 400.0));
        double margin1 = (s1 > s2) ? calculateMargin(s1, s2) : 0.0;
        double delta = k * (margin1 - expected1);
        
        p1.setElo(p1.getElo() + (int)Math.max(2.0, delta));
        p2.setElo(p2.getElo() - (int)Math.max(2.0, delta));

        playerRepository.save(p1);
        playerRepository.save(p2);
    }

    @PostMapping("/matches/generate")
    @Transactional
    public String generateMatches(@RequestParam("matchDate") String matchDate, 
                                @RequestParam(value = "selectedPlayerNames") List<String> selectedNames) {

        matchRepository.deleteByMatchDate(matchDate);

        List<String> shuffledNames = new ArrayList<>(selectedNames);
        java.util.Collections.shuffle(shuffledNames);
        
        for (int i = 0; i < shuffledNames.size(); i++) {
            for (int j = i + 1; j < shuffledNames.size(); j++) {
                Match match = new Match();
                match.setPlayer1(shuffledNames.get(i));
                match.setPlayer2(shuffledNames.get(j));
                match.setMatchDate(matchDate);
                // ... 생략
                matchRepository.save(match);
            }
        }
        return "redirect:/matches?date=" + matchDate;
    }
       

    @PostMapping("/matches/save-matrix")
    @Transactional
    public String saveMatrix(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Set<Long> matchIds = new HashSet<>();
        for (String key : paramMap.keySet()) {
            if (key.startsWith("s1_") || key.startsWith("s2_")) matchIds.add(Long.parseLong(key.split("_")[1]));
        }

        String matchDate = "";
        for (Long matchId : matchIds) {
            Match match = matchRepository.findById(matchId).orElseThrow();
            matchDate = match.getMatchDate();
            String s1Val = request.getParameter("s1_" + matchId);
            String s2Val = request.getParameter("s2_" + matchId);
            match.setScore1(Integer.parseInt(s1Val != null ? s1Val : "0"));
            match.setScore2(Integer.parseInt(s2Val != null ? s2Val : "0"));
            match.setFinished(true);
            updateStats(match);
            matchRepository.save(match);
        }
        return "redirect:/matches?date=" + matchDate;
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
        if (diff >= 3) return 1.5;
        if (diff == 2) return 1.2;
        return 1.0;
    }

    @GetMapping("/matches/recalculate-all")
    @Transactional
    public String recalculateAll(@RequestParam("date") String date) {
        List<Match> matches = matchRepository.findByMatchDate(date);
        for (Match match : matches) {
            if (match.isFinished()) {
                updateStats(match); // 이 메서드가 Elo를 계산하고 저장합니다!
            }
        }
        return "redirect:/matches?date=" + date;
    }
}