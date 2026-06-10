package com.mg.pingpong.service;

import com.mg.pingpong.entity.Match;
import com.mg.pingpong.entity.Player;
import com.mg.pingpong.repository.MatchRepository;
import com.mg.pingpong.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EloCalculationService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    // 전체 match 기록으로 모든 선수 ELO 계산
    public Map<String, Integer> calculateAllElo() {
        List<Match> allMatches = matchRepository.findAllByFinishedTrueOrderByIdAsc();
        List<Player> allPlayers = playerRepository.findAll();

        // 초기 ELO 1500으로 세팅
        Map<String, Integer> eloMap = new HashMap<>();
        Map<String, String> gradeMap = new HashMap<>();
        for (Player p : allPlayers) {
            eloMap.put(p.getName(), 1500);
            gradeMap.put(p.getName(), p.getGrade());
        }

        // 경기 순서대로 ELO 계산
        for (Match match : allMatches) {
            String name1 = match.getPlayer1();
            String name2 = match.getPlayer2();
            int s1 = match.getScore1();
            int s2 = match.getScore2();

            int elo1 = eloMap.getOrDefault(name1, 1500);
            int elo2 = eloMap.getOrDefault(name2, 1500);
            String grade1 = gradeMap.getOrDefault(name1, null);
            String grade2 = gradeMap.getOrDefault(name2, null);

            double r1 = elo1 + getDivisionScore(grade1) * 0.5;
            double r2 = elo2 + getDivisionScore(grade2) * 0.5;

            double expected1 = 1.0 / (1.0 + Math.pow(10, (r2 - r1) / 400.0));
            double expected2 = 1.0 - expected1;

            double actual1, actual2;
            if (s1 > s2)      { actual1 = calculateMargin(s1, s2); actual2 = 0.0; }
            else if (s2 > s1) { actual1 = 0.0; actual2 = calculateMargin(s2, s1); }
            else              { actual1 = 0.5; actual2 = 0.5; }

            double k = 32.0;
            int delta1 = (int)(k * (actual1 - expected1));
            int delta2 = (int)(k * (actual2 - expected2));

            if (s1 > s2)      { delta1 = Math.max(2, delta1); delta2 = Math.min(-2, delta2); }
            else if (s2 > s1) { delta2 = Math.max(2, delta2); delta1 = Math.min(-2, delta1); }

            eloMap.put(name1, elo1 + delta1);
            eloMap.put(name2, elo2 + delta2);
        }

        return eloMap;
    }

    // 전체 match 기록으로 승/패 계산
    public Map<String, int[]> calculateWinLose() {
        List<Match> allMatches = matchRepository.findAllByFinishedTrueOrderByIdAsc();
        Map<String, int[]> winLoseMap = new HashMap<>();

        for (Match match : allMatches) {
            winLoseMap.putIfAbsent(match.getPlayer1(), new int[]{0, 0});
            winLoseMap.putIfAbsent(match.getPlayer2(), new int[]{0, 0});

            if (match.getScore1() > match.getScore2()) {
                winLoseMap.get(match.getPlayer1())[0]++;  // p1 승
                winLoseMap.get(match.getPlayer2())[1]++;  // p2 패
            } else if (match.getScore2() > match.getScore1()) {
                winLoseMap.get(match.getPlayer2())[0]++;  // p2 승
                winLoseMap.get(match.getPlayer1())[1]++;  // p1 패
            }
        }
        return winLoseMap;
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
}