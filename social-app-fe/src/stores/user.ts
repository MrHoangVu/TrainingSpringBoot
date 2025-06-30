// src/stores/user.ts

import { defineStore } from 'pinia'
import { ref } from 'vue'
import userApi from '@/api/userApi'
import type { UserProfile, UpdateProfileRequest } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  // STATE
  const currentUser = ref<UserProfile | null>(null)
  const isLoading = ref(false)

  async function fetchCurrentUser() {
    //Nếu đã có thông tin user rồi thì không cần fetch lại
    if (currentUser.value) return

    try {
      const response = await userApi.getCurrentUser()
      currentUser.value = response.data
    } catch (error) {
      console.error("Failed to fetch user:", error)
      currentUser.value = null
      // Nếu token hết hạn hoặc không hợp lệ, API sẽ báo lỗi 401/403
      // Lúc này có thể cần đăng xuất người dùng
      // const authStore = useAuthStore()
      // authStore.logout()
    }
  }

  function clearUser() {
    currentUser.value = null
  }

  async function updateUserProfile(payload: UpdateProfileRequest) {
    isLoading.value = true
    try {
      const response = await userApi.updateProfile(payload)
      currentUser.value = response.data // Cập nhật lại thông tin user trong store
    } catch (error) {
      console.error("Failed to update profile:", error)
      throw error // Ném lỗi ra để component có thể bắt và xử lý
    } finally {
      isLoading.value = false
    }
  }

  async function updateUserAvatar(file: File) {
    isLoading.value = true
    try {
      const response = await userApi.updateAvatar(file)
      if (currentUser.value) {
        // Cập nhật lại URL avatar trong store
        currentUser.value.avatarUrl = response.data.avatarUrl
      }
    } catch (error) {
      console.error("Failed to update avatar:", error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  return {
    currentUser,
    isLoading,
    fetchCurrentUser,
    clearUser,
    updateUserProfile,
    updateUserAvatar,
  }
})