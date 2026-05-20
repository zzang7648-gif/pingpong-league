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
    private String grade;
    private int win;
    private int lose;
    
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getGrade() {
        return grade;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public int getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public int getLose() {
        return lose;
    }

    public void setLose(int lose) {
        this.lose = lose;
    }
    
}