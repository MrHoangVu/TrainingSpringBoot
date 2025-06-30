// src/types/api.ts

// Dựa trên UserProfileResponse.java
export interface UserProfile {
    id: number;
    email: string;
    fullName: string | null;
    dateOfBirth: string | null; // LocalDate ở backend -> string 'YYYY-MM-DD' ở frontend
    occupation: string | null;
    address: string | null;
    avatarUrl: string | null;
}

// Dựa trên AuthResponse.java
export interface AuthResponse {
    accessToken: string;
}

// Dựa trên UpdateProfileRequest.java
export interface UpdateProfileRequest {
    fullName: string | null;
    dateOfBirth: string | null;
    occupation: string | null;
    address: string | null;
}

// Dựa trên PostResponse.java
export interface Post {
    id: number;
    content: string | null;
    imageUrl: string | null;
    createdAt: string; // LocalDateTime -> string
    updatedAt: string;
    author: UserProfile;
    likeCount: number;
    commentCount: number;
    isLiked?: boolean;
    // Sẽ thêm các trường isLiked, isCommented ở FE sau
}

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number; // số trang hiện tại (bắt đầu từ 0)
    size: number;
    first: boolean;
    last: boolean;
}
export interface Comment {
    id: number;
    content: string;
    createdAt: string;
    updatedAt: string;
    author: UserProfile; // Tái sử dụng UserProfile
}
export type FriendshipStatus = "SELF" | "NONE" | "FRIENDS" | "PENDING_SENT" | "PENDING_RECEIVED" | "BLOCKED_BY_ME" | "BLOCKED_BY_OTHER";

export interface FriendshipStatusResponse {
    status: FriendshipStatus;
}