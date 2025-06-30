package com.example.socialnetwork.unit.service;

import com.example.socialnetwork.entity.Post;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.PostLikeRepository;
import com.example.socialnetwork.repository.PostRepository;
import com.example.socialnetwork.repository.UserRepository;
import com.example.socialnetwork.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeService likeService;

    private User currentUser;
    private Post post;

    @BeforeEach
    void setUp() {
        // CHỈ KHỞI TẠO DỮ LIỆU, KHÔNG STUB GÌ Ở ĐÂY
        currentUser = User.builder().id(1L).email("current@user.com").build();
        post = Post.builder().id(101L).build();
    }

    private void mockSecurityContext() {
        // Tạo một helper method để mock security context khi cần
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser.getEmail(), null)
        );
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
    }

    @Test
    void toggleLike_WhenNotLiked_ShouldLikePost() {
        // Given: Định nghĩa tất cả các stub cần thiết cho bài test này
        mockSecurityContext();
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postLikeRepository.existsById(any())).thenReturn(false);
        when(postLikeRepository.countByPostId(anyLong())).thenReturn(1L);

        // When
        Map<String, Object> result = likeService.toggleLike(post.getId());

        // Then
        verify(postLikeRepository, times(1)).save(any());
        assertTrue((Boolean) result.get("isLiked"));
        assertEquals(1L, result.get("likeCount"));
    }

    @Test
    void toggleLike_WhenAlreadyLiked_ShouldUnlikePost() {
        // Given: Định nghĩa tất cả các stub cần thiết cho bài test này
        mockSecurityContext();
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postLikeRepository.existsById(any())).thenReturn(true);
        when(postLikeRepository.countByPostId(anyLong())).thenReturn(0L);

        // When
        Map<String, Object> result = likeService.toggleLike(post.getId());

        // Then
        verify(postLikeRepository, times(1)).deleteById(any());
        assertFalse((Boolean) result.get("isLiked"));
        assertEquals(0L, result.get("likeCount"));
    }

    @Test
    void toggleLike_PostNotFound_ThrowsException() {
        // Given: Định nghĩa tất cả các stub cần thiết cho bài test này
        mockSecurityContext();
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            likeService.toggleLike(999L);
        });
        assertEquals("Post not found with id: 999", exception.getMessage());
    }
}