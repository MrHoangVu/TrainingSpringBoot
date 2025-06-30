package com.example.socialnetwork.integration.controller;

import com.example.socialnetwork.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/timeline-test-data.sql")
class TimelineControllerTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "current_user@test.com")
    void getTimeline_ShouldReturnPostsOfSelfAndFriends_OrderedByDate() throws Exception {
        mockMvc.perform(get("/api/timeline")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                // Phải có 2 bài viết (của mình và của bạn)
                .andExpect(jsonPath("$.content", hasSize(2)))
                // Bài viết của friend_user mới hơn, phải đứng đầu
                .andExpect(jsonPath("$.content[0].id").value(102))
                .andExpect(jsonPath("$.content[0].author.email").value("friend_user@test.com"))
                // Bài viết của mình cũ hơn, đứng thứ hai
                .andExpect(jsonPath("$.content[1].id").value(101))
                .andExpect(jsonPath("$.content[1].author.email").value("current_user@test.com"));
    }

    @Test
    @WithMockUser(username = "other_user@test.com")
    void getTimeline_ForUserWithNoFriends_ShouldReturnOnlyOwnPosts() throws Exception {
        mockMvc.perform(get("/api/timeline"))
                .andExpect(status().isOk())
                // Chỉ có 1 bài viết của chính mình
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(103));
    }
}