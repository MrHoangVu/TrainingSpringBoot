package com.example.socialnetwork.integration.controller;

import com.example.socialnetwork.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/friendship-test-data.sql")
class FriendshipControllerTest extends AbstractIntegrationTest {

    private final long USER1_ID = 1L;
    private final long USER2_ID = 2L;
    private final long USER3_ID = 3L;

    @Test
    @WithMockUser(username = "user1@test.com")
    void sendFriendRequest_ToNewUser_Success() throws Exception {
        // Gửi yêu cầu đến user3, người chưa có quan hệ gì
        mockMvc.perform(post("/api/friends/request/{recipientId}", USER3_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void acceptFriendRequest_Success() throws Exception {
        // Chấp nhận yêu cầu từ user2 (đã được tạo trong file SQL)
        mockMvc.perform(post("/api/friends/accept/{requesterId}", USER2_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void getPendingFriendRequests_ReturnsRequester() throws Exception {
        // user1 đăng nhập và xem lời mời từ user2
        mockMvc.perform(get("/api/friends/requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int)USER2_ID)));
    }

    // Thêm các test case này vào file FriendshipControllerTest.java đã có

    @Test
    @WithMockUser(username = "user1@test.com")
    void blockUser_Success() throws Exception {
        // user1 chặn user3
        mockMvc.perform(post("/api/friends/block/{userIdToBlock}", USER3_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void unblockUser_Success() throws Exception {
        // Given: user1 đã chặn user2 (quan hệ PENDING ban đầu bị ghi đè)
        mockMvc.perform(post("/api/friends/block/{userIdToBlock}", USER2_ID));

        // When: user1 bỏ chặn user2
        mockMvc.perform(post("/api/friends/unblock/{userIdToUnblock}", USER2_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user2@test.com") // user2 là người gửi yêu cầu ban đầu
    void cancelFriendRequest_Success() throws Exception {
        // user2 hủy yêu cầu đã gửi cho user1
        mockMvc.perform(post("/api/friends/cancel/{recipientId}", USER1_ID))
                .andExpect(status().isOk());
    }
}