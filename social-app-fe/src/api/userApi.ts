import apiClient from './axios'
import type { UpdateProfileRequest, UserProfile } from '@/types/api'
import type { FriendshipStatusResponse } from '@/types/api'

export default {
  getCurrentUser() {
    return apiClient.get<UserProfile>('/users/me')
  },

  updateProfile(payload: UpdateProfileRequest) {
    return apiClient.put<UserProfile>('/users/me', payload)
  },

  updateAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)

    return apiClient.post<{ avatarUrl: string }>('/users/me/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  getUserById(userId: number) {
    return apiClient.get<UserProfile>(`/users/${userId}`)
  },
  getFriendshipStatus(userId: number) {
    return apiClient.get<FriendshipStatusResponse>(`/users/${userId}/friendship-status`)
  },
}