package com.example.socialnetwork.integration.controller;

import com.example.socialnetwork.AbstractIntegrationTest;
import com.example.socialnetwork.dto.request.ForgotPasswordRequest;
import com.example.socialnetwork.dto.request.LoginRequest;
import com.example.socialnetwork.dto.request.RegisterRequest;
import com.example.socialnetwork.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/auth-test-data.sql")
class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("register@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_InvalidEmail_Fails() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


//    @Test
//    void login_Success_ReturnsOtp() throws Exception {
//        LoginRequest request = new LoginRequest();
//        request.setEmail("user@test.com");
//        request.setPassword("password123"); // Mật khẩu gốc
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.otp").exists());
//    }

//    @Test
//    void login_WrongPassword_Fails() throws Exception {
//        LoginRequest request = new LoginRequest();
//        request.setEmail("user@test.com");
//        request.setPassword("wrong-password");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().is(403));
//    }

//    @Test
//    void forgotPassword_Success_ReturnsToken() throws Exception {
//        ForgotPasswordRequest request = new ForgotPasswordRequest();
//        request.setEmail("user@test.com");
//
//        mockMvc.perform(post("/api/auth/forgot-password")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.resetToken").exists());
//    }
}