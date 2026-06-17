package com.mg.pingpong.service;

import com.mg.pingpong.entity.Event;
import com.mg.pingpong.entity.Prediction;
import com.mg.pingpong.repository.PredictionRepository;
import com.mg.pingpong.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {
    
    private final PredictionRepository predictionRepository;
    private final EventRepository eventRepository;
    
    // 예측 저장
    public Prediction savePrediction(Long eventId, String userName, String predictedFirst, String predictedSecond) {
        // 중복 체크
        Prediction existing = predictionRepository.findByEventIdAndUserName(eventId, userName);
        if (existing != null) {
            // 기존 예측 업데이트
            existing.setPredictedFirst(predictedFirst);
            existing.setPredictedSecond(predictedSecond);
            return predictionRepository.save(existing);
        }
        
        // 새 예측 생성
        Prediction prediction = new Prediction();
        prediction.setEventId(eventId);
        prediction.setUserName(userName);
        prediction.setPredictedFirst(predictedFirst);
        prediction.setPredictedSecond(predictedSecond);
        prediction.setCreatedAt(LocalDateTime.now());
        prediction.setIsCorrect(false);
        
        return predictionRepository.save(prediction);
    }
    
    // 특정 경기의 모든 예측 조회
    public List<Prediction> getPredictionsByEvent(Long eventId) {
        List<Prediction> predictions = predictionRepository.findByEventId(eventId);
        
        // 경기 결과가 있으면 맞춤 자동 계산
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event != null && event.getFirstPlaceName() != null) {
            predictions.forEach(p -> {
                boolean isCorrect = p.getPredictedFirst().equals(event.getFirstPlaceName()) &&
                                   p.getPredictedSecond().equals(event.getSecondPlaceName());
                p.setIsCorrect(isCorrect);
                predictionRepository.save(p);
            });
        }
        
        return predictions;
    }
    
    // 예측 조회
    public Prediction getPrediction(Long predictionId) {
        return predictionRepository.findById(predictionId)
            .orElseThrow(() -> new RuntimeException("예측을 찾을 수 없습니다"));
    }
    
    // 예측 삭제
    public void deletePrediction(Long predictionId) {
        predictionRepository.deleteById(predictionId);
    }
}