package com.example.socialnetwork.service;

import com.example.socialnetwork.dto.request.UpdatePostRequest;
import com.example.socialnetwork.dto.response.PostResponse;
import com.example.socialnetwork.dto.response.UserProfileResponse;
import com.example.socialnetwork.entity.Post;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.PostRepository;
import com.example.socialnetwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Helper method để lấy user đang đăng nhập
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public PostResponse createPost(String content, MultipartFile image) {
        User currentUser = getCurrentUser();
        Post newPost = new Post();
        newPost.setUser(currentUser);
        newPost.setContent(content);

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorageService.storeFile(image);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            newPost.setImageUrl(fileDownloadUri);
        }

        Post savedPost = postRepository.save(newPost);
        return mapPostToPostResponse(savedPost);
    }

    public PostResponse updatePost(Long postId, UpdatePostRequest request) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Kiểm tra quyền: chỉ chủ bài viết mới được sửa
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this post.");
        }

        post.setContent(request.getContent());
        Post updatedPost = postRepository.save(post);
        return mapPostToPostResponse(updatedPost);
    }

    public void deletePost(Long postId) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Kiểm tra quyền: chỉ chủ bài viết mới được xóa
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this post.");
        }

        postRepository.delete(post);
    }

    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return mapPostToPostResponse(post);
    }

    // Helper method để map Post Entity sang PostResponse DTO
    public PostResponse mapPostToPostResponse(Post post) {
        User author = post.getUser();
        UserProfileResponse authorProfile = UserProfileResponse.builder()
                .id(author.getId())
                .email(author.getEmail())
                .fullName(author.getFullName())
                .avatarUrl(author.getAvatarUrl())
                .build();

        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .author(authorProfile)
                .likeCount(post.getLikes().size())
                .commentCount(post.getComments().size())
                .build();
    }
}