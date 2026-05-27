package com.mg.pingpong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }
    @GetMapping("/intro")
    public String intro() {
        return "intro"; // templates/intro.html
    }
}