package com.example.socialnetwork.integration.controller;

import com.example.socialnetwork.AbstractIntegrationTest;
import com.example.socialnetwork.dto.request.UpdatePostRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/post-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PostControllerTest extends AbstractIntegrationTest {

    private final long POST_ID_OF_USER1 = 101L;
    private final long POST_ID_OF_USER2 = 102L;

    @Test
    @WithMockUser(username = "user1@test.com")
    void createPost_WithContentAndImage_Success() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("image", "test.png", MediaType.IMAGE_PNG_VALUE, "test-image".getBytes());

        mockMvc.perform(multipart("/api/posts")
                        .file(imageFile)
                        .param("content", "A new post with an image"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("A new post with an image"))
                .andExpect(jsonPath("$.imageUrl", notNullValue()))
                .andExpect(jsonPath("$.author.email").value("user1@test.com"));
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void createPost_WithoutContentOrImage_Fails() throws Exception {
        mockMvc.perform(multipart("/api/posts"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void getPostById_Success() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}", POST_ID_OF_USER1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(POST_ID_OF_USER1))
                .andExpect(jsonPath("$.content").value("Post 1 by User 1"));
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void updatePost_AsOwner_Success() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setContent("Updated content for Post 1");

        mockMvc.perform(put("/api/posts/{postId}", POST_ID_OF_USER1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content for Post 1"));
    }

//    @Test
//    @WithMockUser(username = "user1@test.com") // User 1 tries to update post of User 2
//    void updatePost_NotOwner_Forbidden() throws Exception {
//        UpdatePostRequest request = new UpdatePostRequest();
//        request.setContent("Attempt to update");
//
//        mockMvc.perform(put("/api/posts/{postId}", POST_ID_OF_USER2)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isForbidden());
//    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void deletePost_AsOwner_Success() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", POST_ID_OF_USER1))
                .andExpect(status().isNoContent());
    }

//    @Test
//    @WithMockUser(username = "user1@test.com") // User 1 tries to delete post of User 2
//    void deletePost_NotOwner_Forbidden() throws Exception {
//        mockMvc.perform(delete("/api/posts/{postId}", POST_ID_OF_USER2))
//                .andExpect(status().isForbidden());
//    }

    @Test
    @WithMockUser(username = "user2@test.com")
    void toggleLike_Success() throws Exception {
        // Like
        mockMvc.perform(post("/api/posts/{postId}/like", POST_ID_OF_USER1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1));

        // Unlike
        mockMvc.perform(post("/api/posts/{postId}/like", POST_ID_OF_USER1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(false))
                .andExpect(jsonPath("$.likeCount").value(0));
    }
}