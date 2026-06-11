package com.mg.pingpong.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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
import com.mg.pingpong.service.EloCalculationService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;

@Controller
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private EloCalculationService eloCalculationService;

    @GetMapping("/matches")
    public String matches(Model model, @RequestParam(value = "date", required = false) String date) {
        String targetDate = (date != null) ? date : java.time.LocalDate.now().toString();

        List<Player> allPlayers = playerRepository.findAll();
        List<Match> matches = matchRepository.findByMatchDate(targetDate);

        // ✅ match 기록으로 ELO/승패 실시간 계산
        Map<String, Integer> eloMap = eloCalculationService.calculateAllElo();
        Map<String, int[]> winLoseMap = eloCalculationService.calculateWinLose();

        // ✅ TOP10: ELO 기준 정렬 (경기 기록 있는 선수만)
        List<Map<String, Object>> topPlayers = allPlayers.stream()
            .filter(p -> winLoseMap.containsKey(p.getName()))
            .map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", p.getName());
                m.put("elo", eloMap.getOrDefault(p.getName(), 1500));
                int[] wl = winLoseMap.getOrDefault(p.getName(), new int[]{0, 0});
                m.put("win", wl[0]);
                m.put("lose", wl[1]);
                return m;
            })
            .sorted((a, b) -> (int) b.get("elo") - (int) a.get("elo"))
            .limit(10)
            .toList();

        // 참가자 추출
        LinkedHashSet<String> participantSet = new LinkedHashSet<>();
        for (Match m : matches) {
            participantSet.add(m.getPlayer1());
            participantSet.add(m.getPlayer2());
        }

        List<String> participantNames = new ArrayList<>(participantSet);

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

        Map<String, Match> matchMap = new HashMap<>();
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

        // ✅ players 테이블 건드리지 않고 match만 저장
        match.setScore1(score1);
        match.setScore2(score2);
        match.setFinished(true);
        matchRepository.save(match);

        syncPlayerElos();

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

            if (!match.isFinished()) {
                match.setScore1(scores1.get(i));
                match.setScore2(scores2.get(i));
                match.setFinished(true);
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

            if (!match.isFinished()) {
                String s1Val = request.getParameter("s1_" + matchId);
                String s2Val = request.getParameter("s2_" + matchId);

                if (s1Val == null || s2Val == null || s1Val.isEmpty() || s2Val.isEmpty()) continue;

                match.setScore1(Integer.parseInt(s1Val));
                match.setScore2(Integer.parseInt(s2Val));
                match.setFinished(true);
                matchRepository.save(match);
            }
        }
        syncPlayerElos();


        return "redirect:/matches?date=" + matchDate;
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

    // ✅ recalculate-all 불필요하지만 혹시 몰라 리다이렉트만 남김
    @GetMapping("/matches/recalculate-all")
    public String recalculateAll(@RequestParam("date") String date) {
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

    @Transactional
    private void syncPlayerElos() {
        // 1. EloCalculationService에서 계산된 ELO 가져오기
        Map<String, Integer> eloMap = eloCalculationService.calculateAllElo();
        Map<String, int[]> winLoseMap = eloCalculationService.calculateWinLose();
        
        // 2. 모든 플레이어 조회
        List<Player> allPlayers = playerRepository.findAll();
        
        // 3. 각 플레이어의 ELO/승패 업데이트
        for (Player player : allPlayers) {
            String playerName = player.getName();
            
            if (eloMap.containsKey(playerName)) {
                player.setElo(eloMap.get(playerName));
            }
            
            if (winLoseMap.containsKey(playerName)) {
                int[] wl = winLoseMap.get(playerName);
                player.setWin(wl[0]);
                player.setLose(wl[1]);
            }
            
            playerRepository.save(player);
        }
        
        // 즉시 DB 반영
        playerRepository.flush();
    }
}