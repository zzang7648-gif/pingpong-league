package com.mg.pingpong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.mg.pingpong.repository.PlayerRepository;
import com.mg.pingpong.repository.MatchRepository;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import com.mg.pingpong.entity.Player;
import com.mg.pingpong.entity.Match;

// AdminController.java
@Controller
public class AdminController {

    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private MatchRepository matchRepository;

    // 관리자 로그인
    @PostMapping("/admin/login")
    public String login(@RequestParam("password") String password, HttpSession session) {
        if ("1130".equals(password)) {  // 간단한 비밀번호
            session.setAttribute("isAdmin", true);
            return "redirect:/admin/dashboard";
        }
        return "redirect:/?error=비밀번호가틀렸습니다";
    }

    // 관리자 로그아웃
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("isAdmin");
        return "redirect:/";
    }

    // 관리자 대시보드
    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/?error=권한이없습니다";
        }
        
        List<Player> players = playerRepository.findAll();
        List<Match> matches = matchRepository.findAll();
        
        model.addAttribute("players", players);
        model.addAttribute("matches", matches);
        
        return "admin-dashboard";
    }

    // 선수 삭제 (관리자만)
    @PostMapping("/admin/players/delete")
    public String deletePlayer(@RequestParam("id") Long id, HttpSession session) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/?error=권한이없습니다";
        }
        playerRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // 경기 초기화 (관리자만)
    @PostMapping("/admin/matches/delete-all")
    public String deleteAllMatches(@RequestParam("date") String date, HttpSession session) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/?error=권한이없습니다";
        }
        matchRepository.deleteByMatchDate(date);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/login")
    public String loginPage() {
    return "adminlogin"; // templates 폴더에 admin-login.html 파일이 있어야 합니다.
}
}