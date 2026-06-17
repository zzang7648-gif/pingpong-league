package com.mg.pingpong.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prediction")
public class Prediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_id")
    private Long eventId;
    
    @Column(name = "user_name", length = 50)
    private String userName;
    
    @Column(name = "predicted_first", length = 50)
    private String predictedFirst;
    
    @Column(name = "predicted_second", length = 50)
    private String predictedSecond;
    
    @Column(name = "is_correct")
    private Boolean isCorrect = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Getter Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getPredictedFirst() { return predictedFirst; }
    public void setPredictedFirst(String predictedFirst) { this.predictedFirst = predictedFirst; }
    
    public String getPredictedSecond() { return predictedSecond; }
    public void setPredictedSecond(String predictedSecond) { this.predictedSecond = predictedSecond; }
    
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}