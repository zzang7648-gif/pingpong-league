package com.mg.pingpong.controller;

import com.mg.pingpong.entity.Weather;
import com.mg.pingpong.repository.WeatherRepository;
import com.mg.pingpong.service.WeatherApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class WeatherTestController {

    private final WeatherApiService weatherApiService;
    private final WeatherRepository weatherRepository;

    public WeatherTestController(WeatherApiService weatherApiService, WeatherRepository weatherRepository) {
        this.weatherApiService = weatherApiService;
        this.weatherRepository = weatherRepository;
    }

    @GetMapping("/weather")
    public String showWeather(Model model) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate today = LocalDate.now();

        String start = today.minusDays(1).format(fmt);
        String end   = today.plusDays(5).format(fmt);

        List<Weather> weatherList = weatherRepository.findByTargetDateBetween(start, end);

        // ✅ 요일 매핑 추가
        Map<String, String> weekdayMap = new LinkedHashMap<>();
        String[] weekdays = {"일", "월", "화", "수", "목", "금", "토"};
        for (Weather w : weatherList) {
            LocalDate date = LocalDate.parse(w.getTargetDate(), fmt);
            String weekday = weekdays[date.getDayOfWeek().getValue() % 7]; // 일=0 맞춤
            weekdayMap.put(w.getTargetDate(), weekday);
        }

        model.addAttribute("weatherList", weatherList);
        model.addAttribute("weekdayMap", weekdayMap);
        model.addAttribute("today", today.format(fmt));
        return "weather";
    }

    // ✅ 수동 갱신 엔드포인트
    @GetMapping("/api/refresh-weather")
    @ResponseBody
    public String refreshWeather() {
        weatherApiService.saveWeatherData(); // ✅ 동일한 이름
        return "날씨 데이터 갱신 완료";
    }
    // 원본 JSON 확인용
    @GetMapping("/api/test-weather")
    @ResponseBody
    public String testWeather() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return weatherApiService.fetchForecastData(today);
    }
}