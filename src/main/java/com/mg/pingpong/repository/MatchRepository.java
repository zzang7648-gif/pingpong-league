package com.mg.pingpong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mg.pingpong.entity.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {
    boolean existsByPlayer1AndPlayer2AndMatchDate(String player1, String player2, String matchDate);
    
    List<Match> findByMatchDate(String matchDate);
    void deleteByMatchDate(String matchDate);

    List<Match> findAllByFinishedTrueOrderByIdAsc();

}