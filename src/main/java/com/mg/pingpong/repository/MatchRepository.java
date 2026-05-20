package com.mg.pingpong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mg.pingpong.entity.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {

}