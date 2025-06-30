package com.example.socialnetwork.service;

import com.example.socialnetwork.dto.response.PostResponse;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.FriendshipRepository;
import com.example.socialnetwork.repository.PostRepository;
import com.example.socialnetwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final PostRepository postRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final PostService postService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Page<PostResponse> getTimeline(Pageable pageable) {
        User currentUser = getCurrentUser();

        // 1. Lấy danh sách ID của bạn bè
        List<Long> friendIds = friendshipRepository.findFriendIdsByUserId(currentUser.getId());

        // 2. Bao gồm cả bài viết của chính mình trong timeline
        friendIds.add(currentUser.getId());

        // 3. Lấy các bài viết từ danh sách ID bạn bè
        return postRepository.findPostsByFriendIds(friendIds, pageable)
                .map(postService::mapPostToPostResponse);
    }
}