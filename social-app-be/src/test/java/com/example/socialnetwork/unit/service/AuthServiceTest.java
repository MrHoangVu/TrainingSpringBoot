package com.example.socialnetwork.unit.service;

import com.example.socialnetwork.dto.request.*;
import com.example.socialnetwork.dto.response.AuthResponse;
import com.example.socialnetwork.entity.Role;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.UserRepository;
import com.example.socialnetwork.security.JwtService;
import com.example.socialnetwork.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").password("encodedPass").build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        authService.register(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_EmailInUse_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void login_Success_ReturnsOtp() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        String otp = authService.login(request);

        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void verifyOtp_Success() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("user@test.com");
        request.setOtp("123456");
        user.setOtp("123456");
        user.setOtpGeneratedTime(LocalDateTime.now());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.verifyOtp(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertNull(user.getOtp());
    }

    @Test
    void verifyOtp_InvalidOtp_ThrowsException() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("user@test.com");
        request.setOtp("wrong");
        user.setOtp("123456");
        user.setOtpGeneratedTime(LocalDateTime.now());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.verifyOtp(request));
    }

    @Test
    void verifyOtp_Expired_ThrowsException() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setOtp("123456");
        user.setOtp("123456");
        user.setOtpGeneratedTime(LocalDateTime.now().minusMinutes(10)); // Hết hạn
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.verifyOtp(request));
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@test.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.forgotPassword(request));
    }

    @Test
    void resetPassword_TokenExpired_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        user.setPasswordResetToken("expired-token");
        user.setPasswordResetTokenExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByPasswordResetToken(anyString())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.resetPassword(request));
    }
}