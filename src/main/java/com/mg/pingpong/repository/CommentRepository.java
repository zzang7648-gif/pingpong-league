package com.mg.pingpong.repository;

import com.mg.pingpong.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedDateDesc(Long postId);
}