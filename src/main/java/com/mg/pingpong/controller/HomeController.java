package com.mg.pingpong.controller;

import com.mg.pingpong.entity.Weather;
import com.mg.pingpong.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import com.mg.pingpong.entity.Event;
import com.mg.pingpong.service.EventService;



@Controller
@RequiredArgsConstructor // 생성자 주입을 자동으로 해줍니다.
public class HomeController {

    private final WeatherRepository weatherRepository;
    private final EventService eventService;

    @GetMapping("/")
    public String home(Model model) {
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        List<Weather> list = weatherRepository.findByTargetDateBetween(yesterday, tomorrow);
        
        // 이 로그를 찍었을 때 3이 나와야 합니다.
        System.out.println("조회된 날씨 데이터 개수: " + list.size());
        
        model.addAttribute("threeDaysWeather", list);
        return "home";
    }

    @GetMapping("/intro")
    public String intro() {
        return "intro"; // templates/intro.html
    }

    @GetMapping("/elo")
    public String elo() {
        return "elo";
    }
    @GetMapping("/game")
    public String game() {
        return "redirect:/game.html";
    }

    
    @GetMapping("/predict")
    public String predict() {
        return "redirect:/prediction.html";  // 새 파일로 리다이렉트
    }

    
}