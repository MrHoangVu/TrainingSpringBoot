package com.example.socialnetwork.integration.controller;

import com.example.socialnetwork.AbstractIntegrationTest;
import com.example.socialnetwork.dto.request.CommentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/comment-test-data.sql")
class CommentControllerTest extends AbstractIntegrationTest {

    private final long POST_ID = 101L;
    private final long COMMENT_ID = 1001L;
    private final long USER1_ID = 1L;

    @Test
    @WithMockUser(username = "user1@test.com")
    void createComment_Success() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setContent("A new comment");

        mockMvc.perform(post("/api/posts/{postId}/comments", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("A new comment"))
                .andExpect(jsonPath("$.author.id").value(USER1_ID));
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void getCommentsByPost_Success() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", POST_ID)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(COMMENT_ID));
    }

    @Test
    @WithMockUser(username = "user2@test.com")
    void updateComment_AsOwner_Success() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setContent("Updated comment");

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment"));
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void updateComment_NotOwner_Forbidden() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setContent("Forbidden update");

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user3@test.com")
    void deleteComment_AsUnrelatedUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/comments/{commentId}", COMMENT_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user2@test.com")
    void deleteComment_AsCommentOwner_Success() throws Exception {
        mockMvc.perform(delete("/api/comments/{commentId}", COMMENT_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void deleteComment_AsPostOwner_Success() throws Exception {
        mockMvc.perform(delete("/api/comments/{commentId}", COMMENT_ID))
                .andExpect(status().isNoContent());
    }
}