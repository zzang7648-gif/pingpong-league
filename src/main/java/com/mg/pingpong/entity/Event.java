package com.mg.pingpong.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "event")
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_date")
    private LocalDate eventDate;
    
    @Column(name = "event_name", length = 100)
    private String eventName;
    
    @Column(name = "first_place_name", length = 50)
    private String firstPlaceName;
    
    @Column(name = "second_place_name", length = 50)
    private String secondPlaceName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Getter Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    
    public String getFirstPlaceName() { return firstPlaceName; }
    public void setFirstPlaceName(String firstPlaceName) { this.firstPlaceName = firstPlaceName; }
    
    public String getSecondPlaceName() { return secondPlaceName; }
    public void setSecondPlaceName(String secondPlaceName) { this.secondPlaceName = secondPlaceName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}