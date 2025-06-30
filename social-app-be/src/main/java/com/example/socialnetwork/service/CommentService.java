package com.example.socialnetwork.service;

import com.example.socialnetwork.dto.request.CommentRequest;
import com.example.socialnetwork.dto.response.CommentResponse;
import com.example.socialnetwork.dto.response.UserProfileResponse;
import com.example.socialnetwork.entity.Comment;
import com.example.socialnetwork.entity.Post;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.CommentRepository;
import com.example.socialnetwork.repository.PostRepository;
import com.example.socialnetwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public CommentResponse createComment(Long postId, CommentRequest request) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Comment newComment = new Comment();
        newComment.setContent(request.getContent());
        newComment.setUser(currentUser);
        newComment.setPost(post);

        Comment savedComment = commentRepository.save(newComment);
        return mapCommentToCommentResponse(savedComment);
    }

    public Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }
        Page<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
        return comments.map(this::mapCommentToCommentResponse);
    }

    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this comment.");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return mapCommentToCommentResponse(updatedComment);
    }

    public void deleteComment(Long commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // Chủ bình luận hoặc chủ bài viết có thể xóa bình luận
        if (!comment.getUser().getId().equals(currentUser.getId()) &&
                !comment.getPost().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapCommentToCommentResponse(Comment comment) {
        User author = comment.getUser();
        UserProfileResponse authorProfile = UserProfileResponse.builder()
                .id(author.getId())
                .fullName(author.getFullName())
                .avatarUrl(author.getAvatarUrl())
                .build();

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .author(authorProfile)
                .build();
    }
}