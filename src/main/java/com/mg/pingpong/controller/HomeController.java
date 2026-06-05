package com.mg.pingpong.controller;

import com.mg.pingpong.entity.Weather;
import com.mg.pingpong.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor // 생성자 주입을 자동으로 해줍니다.
public class HomeController {

    private final WeatherRepository weatherRepository;

    @GetMapping("/")
    public String home(Model model) {
        // DB에서 최신 날씨 데이터를 가져와 메인 페이지로 보냄
        Weather latestWeather = weatherRepository.findTopByOrderByIdDesc();
        model.addAttribute("weather", latestWeather);
        return "home"; // index.html 파일을 반환
    }

    @GetMapping("/intro")
    public String intro() {
        return "intro"; // templates/intro.html
    }
}