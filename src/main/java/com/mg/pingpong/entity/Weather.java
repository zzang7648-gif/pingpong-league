package com.mg.pingpong.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String temp;      // 기온
    private String pop;       // 강수확률
    private String skyStatus; // 하늘상태
    private LocalDateTime createdAt; // 저장 시간

    @Builder
    public Weather(String temp, String pop, String skyStatus) {
        this.temp = temp;
        this.pop = pop;
        this.skyStatus = skyStatus;
        this.createdAt = LocalDateTime.now();
    }
}