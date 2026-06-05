package com.mg.pingpong.repository;

import com.mg.pingpong.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    // 최근 저장된 1개를 가져오는 메서드
    Weather findTopByOrderByIdDesc();
}