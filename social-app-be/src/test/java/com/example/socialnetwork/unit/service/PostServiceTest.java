package com.example.socialnetwork.unit.service;

import com.example.socialnetwork.dto.request.UpdatePostRequest;
import com.example.socialnetwork.dto.response.PostResponse;
import com.example.socialnetwork.entity.Post;
import com.example.socialnetwork.entity.Role;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.PostRepository;
import com.example.socialnetwork.repository.UserRepository;
import com.example.socialnetwork.service.FileStorageService;
import com.example.socialnetwork.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").role(Role.ROLE_USER).build();

        // Sửa lỗi NullPointerException bằng cách khởi tạo collection
        post = Post.builder()
                .id(10L)
                .content("Original content")
                .user(user)
                .likes(new HashSet<>())
                .comments(new ArrayList<>())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }



    @Test
    void testCreatePost_WithContentAndImage_Success() {
        // Given
        String content = "Test post";
        MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image content".getBytes());
        String fileName = "unique-file-name.jpg";
        String expectedUrl = "http://localhost/uploads/" + fileName;

        // Bắt đầu mock các phương thức tĩnh của class ServletUriComponentsBuilder
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {

            // 1. Tạo một đối tượng mock có đúng kiểu ServletUriComponentsBuilder
            //    Sử dụng RETURNS_DEEP_STUBS để tự động mock các lời gọi phương thức nối chuỗi (như .path())
            ServletUriComponentsBuilder builderMock = Mockito.mock(ServletUriComponentsBuilder.class, Mockito.RETURNS_DEEP_STUBS);

            // 2. Khi fromCurrentContextPath() được gọi, trả về đối tượng mock của chúng ta
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builderMock);

            // 3. Cấu hình lời gọi cuối cùng .toUriString() trên đối tượng mock để trả về URL mong muốn
            when(builderMock.path(anyString()).path(anyString()).toUriString()).thenReturn(expectedUrl);


            // Cấu hình các mock khác như bình thường
            when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn(fileName);
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PostResponse response = postService.createPost(content, image);

            // Then
            assertNotNull(response);
            assertEquals(content, response.getContent());
            assertEquals(expectedUrl, response.getImageUrl());
            verify(postRepository, times(1)).save(any(Post.class));
        }
    }
    @Test
    void testUpdatePost_Success() {
        // Given
        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setContent("Updated content");
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // When
        PostResponse response = postService.updatePost(post.getId(), updateRequest);

        // Then
        assertEquals("Updated content", response.getContent());
        verify(postRepository, times(1)).save(post);
    }

    // ... các test khác cho PostService ...
}