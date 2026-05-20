package com.mg.pingpong.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "matches")

public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String player1;
    private String player2;
    private int score1;
    private int score2;

    // 경기 완료 여부 추가 (기본값 false)
    private boolean isFinished = false;

    // Getter, Setter
    public Long getId() { return id; }

    public String getPlayer1() { return player1; }
    public void setPlayer1(String player1) { this.player1 = player1; }

    public String getPlayer2() { return player2; }
    public void setPlayer2(String player2) { this.player2 = player2; }

    public int getScore1() { return score1; }
    public void setScore1(int score1) { this.score1 = score1; }

    public int getScore2() { return score2; }
    public void setScore2(int score2) { this.score2 = score2; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }
}