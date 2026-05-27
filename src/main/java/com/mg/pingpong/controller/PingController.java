package com.mg.pingpong.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    // UptimeRobot 등이 5분마다 이 주소를 호출하게 설정하세요.
    // 예: https://your-app.onrender.com/ping
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}