package com.mg.pingpong.controller;

import com.mg.pingpong.entity.Weather;
import com.mg.pingpong.repository.WeatherRepository;
import com.mg.pingpong.service.WeatherApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // @RestController 대신 @Controller 사용 (화면 응답을 위해)
public class WeatherTestController {

    private final WeatherApiService weatherApiService;
    private final WeatherRepository weatherRepository; // 리포지토리 추가

    // 생성자 주입 방식으로 서비스와 리포지토리를 모두 받습니다.
    public WeatherTestController(WeatherApiService weatherApiService, WeatherRepository weatherRepository) {
        this.weatherApiService = weatherApiService;
        this.weatherRepository = weatherRepository;
    }

    // JSON 데이터 확인용
    @GetMapping("/api/test-weather")
    @ResponseBody // 이 메서드는 JSON 문자열을 그대로 반환함
    public String testWeather() {
        return weatherApiService.fetchWeatherData();
    }

    // 날씨 화면 출력용
    @GetMapping("/weather")
    public String showWeather(Model model) {
        // 1. 최신 정보 DB에 저장
        weatherApiService.saveWeatherToDb(); 
        
        // 2. DB에서 가장 최근 데이터 조회
        Weather latestWeather = weatherRepository.findTopByOrderByIdDesc();
        
        // 3. 모델에 담아서 뷰로 전달
        model.addAttribute("weather", latestWeather);
        
        return "weather"; // templates/weather.html 파일을 렌더링
    }
}