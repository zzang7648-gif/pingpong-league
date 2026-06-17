package com.mg.pingpong.controller;

import com.mg.pingpong.entity.Event;
import com.mg.pingpong.entity.Prediction;
import com.mg.pingpong.service.EventService;
import com.mg.pingpong.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PredictionController {
    
    private final EventService eventService;
    private final PredictionService predictionService;
    
    // API: 날짜별 경기 조회
    @GetMapping("/api/events")
    @ResponseBody
    public Map<String, Object> getEvents(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Event> events = eventService.getEventsByDate(date);
        return Map.of("events", events);
    }
    
    // API: 예측 저장
    @PostMapping("/prediction/save")
    @ResponseBody
    public Map<String, Object> savePrediction(
        @RequestParam Long eventId,
        @RequestParam String userName,
        @RequestParam String predictedFirst,
        @RequestParam String predictedSecond) {
        
        try {
            Prediction prediction = predictionService.savePrediction(eventId, userName, predictedFirst, predictedSecond);
            return Map.of("success", true, "message", "예측이 저장되었습니다", "id", prediction.getId());
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    // API: 경기의 예측들 조회
    @GetMapping("/api/event/{eventId}")
    @ResponseBody
    public List<Prediction> getEventPredictions(@PathVariable Long eventId) {
        return predictionService.getPredictionsByEvent(eventId);
    }
    
    // API: 예측 삭제
    @DeleteMapping("/api/prediction/{predictionId}")
    @ResponseBody
    public Map<String, String> deletePrediction(@PathVariable Long predictionId) {
        try {
            predictionService.deletePrediction(predictionId);
            return Map.of("success", "true");
        } catch (Exception e) {
            return Map.of("success", "false");
        }
    }

    // 경기 생성
@PostMapping("/api/event/create")
@ResponseBody
public Map<String, Object> createEvent(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
    @RequestParam String eventName) {
    
    try {
        Event event = eventService.createEvent(eventDate, eventName);
        return Map.of("success", true, "message", "경기가 생성되었습니다", "id", event.getId());
    } catch (Exception e) {
        return Map.of("success", false, "message", e.getMessage());
    }
}

// 경기 결과 입력
@PutMapping("/api/event/{eventId}/result")
@ResponseBody
public Map<String, Object> updateEventResult(
    @PathVariable Long eventId,
    @RequestParam String firstPlace,
    @RequestParam String secondPlace) {
    
    try {
        Event event = eventService.updateEventResult(eventId, firstPlace, secondPlace);
        return Map.of("success", true, "message", "결과가 입력되었습니다");
    } catch (Exception e) {
        return Map.of("success", false, "message", e.getMessage());
    }
}

    // 모든 경기 조회 (관리)
    @GetMapping("/api/all-events")
    @ResponseBody
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @DeleteMapping("/api/event/{eventId}")
    @ResponseBody
    public Map<String, String> deleteEvent(@PathVariable Long eventId) {
        try {
            eventService.deleteEvent(eventId);
            return Map.of("success", "true");
        } catch (Exception e) {
            return Map.of("success", "false");
        }
    }
}