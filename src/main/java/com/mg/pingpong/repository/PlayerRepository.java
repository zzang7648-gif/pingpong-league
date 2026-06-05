package com.mg.pingpong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mg.pingpong.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Player findByName(String name);
    // 이름순 정렬 (추가)
    List<Player> findAllByOrderByNameAsc();
    List<Player> findAllByOrderByGradeAscNameAsc();
    List<Player> findAllByOrderByWinDesc();
    

    List<Player> findTop10ByOrderByEloDesc();

        // Query 어노테이션을 사용하여 승+패가 1 이상인 선수만 가져옵니다.
    @Query("SELECT p FROM Player p WHERE (p.win + p.lose) >= 1 ORDER BY p.elo DESC")
    List<Player> findTop10ActivePlayers();

}