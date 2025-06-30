import apiClient from './axios'

export default {
  login(payload: any) {
    return apiClient.post('/auth/login', payload)
  },
  verifyOtp(payload: any) {
    return apiClient.post('/auth/verify-otp', payload)
  },
  register(payload: any) {
    return apiClient.post('/auth/register', payload)
  },
}