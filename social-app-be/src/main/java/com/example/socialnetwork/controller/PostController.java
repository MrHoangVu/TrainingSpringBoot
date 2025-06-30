package com.example.socialnetwork.controller;

import com.example.socialnetwork.dto.request.UpdatePostRequest;
import com.example.socialnetwork.dto.response.PostResponse;
import com.example.socialnetwork.service.LikeService;
import com.example.socialnetwork.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "3. Post Management", description = "APIs for creating, editing, and deleting posts")
@SecurityRequirement(name = "bearerAuth")
public class PostController {

    private final PostService postService;
    private final LikeService likeService;


    @Operation(summary = "Create a new post")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        if ((content == null || content.isBlank()) && (image == null || image.isEmpty())) {
            throw new IllegalArgumentException("Post must have either content or an image.");
        }

        PostResponse newPost = postService.createPost(content, image);
        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a single post by its ID")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @Operation(summary = "Update an existing post")
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request) {
        PostResponse updatedPost = postService.updatePost(postId, request);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(summary = "Delete a post")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Like or Unlike a post")
    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long postId) {
        Map<String, Object> response = likeService.toggleLike(postId);
        return ResponseEntity.ok(response);
    }
}