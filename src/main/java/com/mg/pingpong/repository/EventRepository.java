package com.mg.pingpong.repository;

import com.mg.pingpong.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventDate(LocalDate eventDate);
    List<Event> findByEventDateBetween(LocalDate startDate, LocalDate endDate);
}