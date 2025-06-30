package com.example.socialnetwork.repository;

import com.example.socialnetwork.entity.PostLike;
import com.example.socialnetwork.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    long countByPostId(Long postId);

    // Đếm số lượt thích mới trên các bài viết của một user trong một khoảng thời gian
    long countByPostUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}