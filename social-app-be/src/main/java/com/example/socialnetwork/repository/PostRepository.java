package com.example.socialnetwork.repository;

import com.example.socialnetwork.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    //dùng cho tính năng Timeline
    @Query("SELECT p FROM Post p WHERE p.user.id IN :friendIds ORDER BY p.createdAt DESC")
    Page<Post> findPostsByFriendIds(@Param("friendIds") List<Long> friendIds, Pageable pageable);

    // Đếm số bài viết của một user trong một khoảng thời gian
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}