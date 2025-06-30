import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import authApi from '@/api/authApi' 
import type { AuthResponse } from '@/types/api'
import { useUserStore } from './user'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()
   const userStore = useUserStore()

  // STATE
  const token = ref<string | null>(localStorage.getItem('accessToken'))
  const error = ref<string | null>(null)
  const isLoading = ref(false)

  // GETTERS
  const isAuthenticated = computed(() => !!token.value)

  // ACTIONS
  async function login(email: string, password: string): Promise<string | null> {
    isLoading.value = true
    error.value = null
    try {
      // API login trả về { message, otp }
      const response = await authApi.login({ email, password })
      // Trả về OTP để component có thể xử lý
      return response.data.otp
    } catch (err: any) {
      error.value = err.response?.data?.error || 'Đã có lỗi xảy ra khi đăng nhập.'
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function verifyOtp(email: string, otp: string) {
    isLoading.value = true
    error.value = null
    try {
      const response = await authApi.verifyOtp({ email, otp })
      const accessToken = (response.data as AuthResponse).accessToken

      // Lưu token vào state và localStorage
      token.value = accessToken
      localStorage.setItem('accessToken', accessToken)
      // GỌI HÀM FETCH USER NGAY SAU KHI CÓ TOKEN
      await userStore.fetchCurrentUser()

      // Chuyển hướng đến trang chủ
      router.push({ name: 'Home' })
    } catch (err: any) {
      error.value = err.response?.data?.error || 'OTP không hợp lệ hoặc đã hết hạn.'
    } finally {
      isLoading.value = false
    }
  }

  function logout() {
    token.value = null
    localStorage.removeItem('accessToken')
     // XÓA THÔNG TIN USER KHI LOGOUT
    userStore.clearUser()
    router.push({ name: 'Login' })
  }

  async function register(email: string, password: string) {
    isLoading.value = true
    error.value = null
    try {
      // API register trả về message
      await authApi.register({ email, password })
      // Sau khi đăng ký thành công, chuyển người dùng đến trang đăng nhập
      router.push({ name: 'Login' })
    } catch (err: any) {
      error.value = err.response?.data?.error || 'Đã có lỗi xảy ra khi đăng ký.'
    } finally {
      isLoading.value = false
    }
  }

  // Thêm hàm clear error để xóa lỗi cũ khi chuyển trang
  function clearError() {
    error.value = null
  }

  return {
    token,
    error,
    isLoading,
    isAuthenticated,
    login,
    verifyOtp,
    logout,
    register, 
    clearError, 
  }
})