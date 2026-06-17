package com.mg.pingpong.service;

import com.mg.pingpong.entity.Event;
import com.mg.pingpong.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    
    private final EventRepository eventRepository;
    
    // 특정 날짜의 경기 조회
    public List<Event> getEventsByDate(LocalDate date) {
        return eventRepository.findByEventDate(date);
    }
    
    // 달력용: 월별 경기 조회
    public List<Event> getEventsByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return eventRepository.findByEventDateBetween(startDate, endDate);
    }
    
    // 경기 생성
    public Event createEvent(LocalDate eventDate, String eventName) {
        Event event = new Event();
        event.setEventDate(eventDate);
        event.setEventName(eventName);
        event.setCreatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }
    
    // 경기 결과 입력 (관리자)
    public Event updateEventResult(Long eventId, String firstPlace, String secondPlace) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다"));
        event.setFirstPlaceName(firstPlace);
        event.setSecondPlaceName(secondPlace);
        return eventRepository.save(event);
    }
    
    // 경기 조회
    public Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다"));
    }
    // 모든 경기 조회
    public List<Event> getAllEvents() {
            return eventRepository.findAll();
        }
        public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }
}