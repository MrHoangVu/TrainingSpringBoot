package com.example.socialnetwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipStatusResponse {
    // Trạng thái quan hệ: NONE, FRIENDS, PENDING_SENT, PENDING_RECEIVED, BLOCKED
    private String status;
}