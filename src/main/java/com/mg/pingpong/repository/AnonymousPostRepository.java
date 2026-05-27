package com.mg.pingpong.repository;

import com.mg.pingpong.entity.AnonymousPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnonymousPostRepository extends JpaRepository<AnonymousPost, Long> {
    // 작성일 기준 최신순으로 정렬해서 가져오기
    List<AnonymousPost> findAllByOrderByCreatedDateDesc();
}