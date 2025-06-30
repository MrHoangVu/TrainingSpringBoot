package com.example.socialnetwork.integration.controller;

import com.example.socialnetwork.AbstractIntegrationTest;
import com.example.socialnetwork.dto.request.UpdateProfileRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/user-test-data.sql")
class UserControllerTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "user1@test.com")
    void getCurrentUser_Success() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@test.com"))
                .andExpect(jsonPath("$.fullName").value("User One"));
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void updateCurrentUser_Success() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setAddress("Hanoi");
        request.setOccupation("Engineer");
        request.setDateOfBirth(LocalDate.of(1999, 1, 1));

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.address").value("Hanoi"));
    }

    @Test
    @WithMockUser(username = "user1@test.com")
    void uploadAvatar_Success() throws Exception {
        MockMultipartFile avatarFile = new MockMultipartFile("file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());

        mockMvc.perform(multipart("/api/users/me/avatar")
                        .file(avatarFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").exists());
    }
}