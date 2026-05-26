package com.mg.pingpong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int grade;
    private int win;
    private int lose;
    private int elo = 1500; // Elo 점수 필드 추가 (기본값 1500)
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getGrade() { return grade; }
    public void setName(String name) { this.name = name; }
    public void setGrade(int grade) { this.grade = grade; }
    public int getWin() { return win; }
    public void setWin(int win) { this.win = win; }
    public int getLose() { return lose; }
    public void setLose(int lose) { this.lose = lose; }

    // Elo 관련 메서드 추가
    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }
    
}