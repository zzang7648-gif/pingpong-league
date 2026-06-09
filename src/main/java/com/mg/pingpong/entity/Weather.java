package com.mg.pingpong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter // <--- 여기가 핵심! API 데이터를 덮어쓰기 위해 필요합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String targetDate;
    private String temp;
    private String pop;
    private String skyStatus;
    private LocalDateTime createdAt;

    @Builder
    public Weather(String targetDate, String temp, String pop, String skyStatus) {
        this.targetDate = targetDate;
        this.temp = temp;
        this.pop = pop;
        this.skyStatus = skyStatus;
        this.createdAt = LocalDateTime.now();
    }
}