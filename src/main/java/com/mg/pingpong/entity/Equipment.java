package com.mg.pingpong.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;  // players 테이블의 name 참조
    private String blade;       // 탁구채
    private String frontRubber; // 앞면 러버
    private String backRubber;  // 뒷면 러버
    private String memo;        // 메모

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getter, Setter
    public Long getId() { return id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getBlade() { return blade; }
    public void setBlade(String blade) { this.blade = blade; }

    public String getFrontRubber() { return frontRubber; }
    public void setFrontRubber(String frontRubber) { this.frontRubber = frontRubber; }

    public String getBackRubber() { return backRubber; }
    public void setBackRubber(String backRubber) { this.backRubber = backRubber; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}