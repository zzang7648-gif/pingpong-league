package com.mg.pingpong.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AnonymousPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content; // 내용이 길어질 수 있으니 TEXT 타입으로 지정
    
    private String nickname;
    private String password; // 추후 수정/삭제 시 사용할 비밀번호
    private LocalDateTime createdDate;

    public AnonymousPost() {
        this.createdDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getCreatedDate() { return createdDate; }
}