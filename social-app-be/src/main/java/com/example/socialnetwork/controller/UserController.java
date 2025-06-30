package com.example.socialnetwork.controller;

import com.example.socialnetwork.dto.request.UpdateProfileRequest;
import com.example.socialnetwork.dto.response.FriendshipStatusResponse;
import com.example.socialnetwork.dto.response.UserProfileResponse;
import com.example.socialnetwork.service.FriendshipService;
import com.example.socialnetwork.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "2. User Management", description = "APIs for managing user profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final FriendshipService friendshipService;

    @Operation(summary = "Get current user's profile")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @Operation(summary = "Update current user's profile")
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
    }

    @Operation(summary = "Upload or update current user's avatar")
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.updateUserAvatar(file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    @Operation(summary = "Get the friendship status with another user")
    @GetMapping("/{userId}/friendship-status")
    public ResponseEntity<FriendshipStatusResponse> getFriendshipStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(friendshipService.getFriendshipStatus(userId));
    }

    // Bổ sung API lấy profile của người khác
    @Operation(summary = "Get a user's profile by ID")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfileById(userId));
    }
}