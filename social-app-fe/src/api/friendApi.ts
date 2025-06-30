import apiClient from './axios'
import type { UserProfile } from '@/types/api'

export default {
    // Lấy danh sách bạn bè
    getList() {
        return apiClient.get<UserProfile[]>('/friends')
    },
    // Lấy danh sách lời mời đang chờ
    getPendingRequests() {
        return apiClient.get<UserProfile[]>('/friends/requests/pending')
    },
    sendRequest(recipientId: number) {
        return apiClient.post(`/friends/request/${recipientId}`)
    },
    acceptRequest(requesterId: number) {
        return apiClient.post(`/friends/accept/${requesterId}`)
    },
    declineRequest(requesterId: number) {
        return apiClient.post(`/friends/decline/${requesterId}`)
    },
    cancelRequest(recipientId: number) {
        return apiClient.post(`/friends/cancel/${recipientId}`)
    },
    unfriend(friendId: number) {
        return apiClient.post(`/friends/unfriend/${friendId}`)
    },
}