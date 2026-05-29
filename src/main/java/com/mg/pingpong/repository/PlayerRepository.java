package com.mg.pingpong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mg.pingpong.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Player findByName(String name);
    // 이름순 정렬 (추가)
    List<Player> findAllByOrderByNameAsc();
    List<Player> findAllByOrderByGradeAscNameAsc();
    List<Player> findAllByOrderByWinDesc();
    

    List<Player> findTop10ByOrderByEloDesc();

}