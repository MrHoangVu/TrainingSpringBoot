package com.example.socialnetwork.controller;

import com.example.socialnetwork.dto.response.UserProfileResponse;
import com.example.socialnetwork.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "5. Friendship Management", description = "APIs for managing friendships")
@SecurityRequirement(name = "bearerAuth")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Operation(summary = "Send a friend request to another user")
    @PostMapping("/request/{recipientId}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable Long recipientId) {
        friendshipService.sendFriendRequest(recipientId);
        return ResponseEntity.ok("Friend request sent successfully.");
    }

    @Operation(summary = "Accept a friend request from another user")
    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<String> acceptFriendRequest(@PathVariable Long requesterId) {
        friendshipService.acceptFriendRequest(requesterId);
        return ResponseEntity.ok("Friend request accepted.");
    }

    @Operation(summary = "Decline a friend request")
    @PostMapping("/decline/{requesterId}")
    public ResponseEntity<String> declineFriendRequest(@PathVariable Long requesterId) {
        friendshipService.declineFriendRequest(requesterId);
        return ResponseEntity.ok("Friend request declined.");
    }

    @PostMapping("/cancel/{recipientId}")
    public ResponseEntity<String> cancelFriendRequest(@PathVariable Long recipientId) {
        friendshipService.cancelFriendRequest(recipientId);
        return ResponseEntity.ok("Friend request canceled.");
    }

    @PostMapping("/block/{userIdToBlock}")
    public ResponseEntity<String> blockUser(@PathVariable Long userIdToBlock) {
        friendshipService.blockUser(userIdToBlock);
        return ResponseEntity.ok("User blocked successfully.");
    }

    @PostMapping("/unblock/{userIdToUnblock}")
    public ResponseEntity<String> unblockUser(@PathVariable Long userIdToUnblock) {
        friendshipService.unblockUser(userIdToUnblock);
        return ResponseEntity.ok("User unblocked successfully.");
    }

    @Operation(summary = "Unfriend a user")
    @PostMapping("/unfriend/{friendId}")
    public ResponseEntity<String> unfriend(@PathVariable Long friendId) {
        friendshipService.unfriend(friendId);
        return ResponseEntity.ok("Successfully unfriended the user.");
    }

    @Operation(summary = "Get the current user's friend list")
    @GetMapping("")
    public ResponseEntity<List<UserProfileResponse>> getFriendList() {
        return ResponseEntity.ok(friendshipService.getFriendList());
    }

    @Operation(summary = "Get pending friend requests received by the current user")
    @GetMapping("/requests/pending")
    public ResponseEntity<List<UserProfileResponse>> getPendingRequests() {
        return ResponseEntity.ok(friendshipService.getPendingFriendRequests());
    }
}
