package com.mg.pingpong.repository;

import com.mg.pingpong.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // [핵심] 이 줄이 있어야 List를 인식합니다!

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    
    // 최근 저장된 1개를 가져오는 메서드
    Weather findTopByOrderByIdDesc();

    // 날짜 범위 조회 (오늘~내일... 일주일치)
    List<Weather> findByTargetDateBetween(String start, String end);
}