package com.mg.pingpong.controller;

import com.mg.pingpong.entity.AnonymousPost;
import com.mg.pingpong.repository.AnonymousPostRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final AnonymousPostRepository boardRepository;

    public BoardController(AnonymousPostRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // 게시판 목록 보기
    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", boardRepository.findAllByOrderByCreatedDateDesc());
        return "board/list";
    }

    // 글 쓰기 페이지
    @GetMapping("/write")
    public String writeForm() {
        return "board/write";
    }

    // 글 저장하기
    @PostMapping("/write")
    public String write(@ModelAttribute AnonymousPost post) {
        boardRepository.save(post);
        return "redirect:/board";
    }

    // 게시글 상세 보기
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        AnonymousPost post = boardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        model.addAttribute("post", post);
        return "board/detail"; // board/detail.html을 만들 예정입니다.
    }   
}