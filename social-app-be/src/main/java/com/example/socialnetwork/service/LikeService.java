package com.example.socialnetwork.service;

import com.example.socialnetwork.entity.Post;
import com.example.socialnetwork.entity.PostLike;
import com.example.socialnetwork.entity.PostLikeId;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.PostLikeRepository;
import com.example.socialnetwork.repository.PostRepository;
import com.example.socialnetwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Map<String, Object> toggleLike(Long postId) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        PostLikeId likeId = new PostLikeId(currentUser.getId(), postId);
        boolean isLiked;

        if (postLikeRepository.existsById(likeId)) {
            // Đã like -> unlike
            postLikeRepository.deleteById(likeId);
            isLiked = false;
        } else {
            // Chưa like -> like
            PostLike newLike = PostLike.builder()
                    .user(currentUser)
                    .post(post)
                    .build();
            postLikeRepository.save(newLike);
            isLiked = true;
        }

        long likeCount = postLikeRepository.countByPostId(postId);

        return Map.of(
                "message", isLiked ? "Post liked successfully" : "Post unliked successfully",
                "isLiked", isLiked,
                "likeCount", likeCount
        );
    }
}