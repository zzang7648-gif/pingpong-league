package com.mg.pingpong.controller;

import com.mg.pingpong.entity.*;
import com.mg.pingpong.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/board")
public class BoardController {
    private final AnonymousPostRepository postRepo;
    private final CommentRepository commentRepo;

    public BoardController(AnonymousPostRepository postRepo, CommentRepository commentRepo) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", postRepo.findAllByOrderByCreatedDateDesc());
        return "board/list";
    }

    @GetMapping("/write")
    public String writeForm() { return "board/write"; }

    @PostMapping("/write")
    public String write(@ModelAttribute AnonymousPost post) {
        postRepo.save(post);
        return "redirect:/board";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AnonymousPost post = postRepo.findById(id).orElseThrow();
        model.addAttribute("post", post);
        model.addAttribute("comments", commentRepo.findByPostIdOrderByCreatedDateDesc(id));
        return "board/detail";
    }

    @PostMapping("/comment/write")
    public String writeComment(@RequestParam Long postId, @ModelAttribute Comment comment) {
        AnonymousPost post = postRepo.findById(postId).orElseThrow();
        comment.setPost(post);
        commentRepo.save(comment);
        return "redirect:/board/" + postId;
    }
}