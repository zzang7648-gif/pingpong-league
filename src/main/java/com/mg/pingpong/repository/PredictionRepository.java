package com.mg.pingpong.repository;

import com.mg.pingpong.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByEventId(Long eventId);
    Prediction findByEventIdAndUserName(Long eventId, String userName);
}