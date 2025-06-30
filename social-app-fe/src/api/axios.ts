import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use(
  (config) => {
    // Lấy token trực tiếp từ localStorage
    const token = localStorage.getItem('accessToken')

    if (token) {
      // Gắn token vào header Authorization
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('accessToken')
      window.location.href = '/auth/login';
    }
    return Promise.reject(error)
  }
)

export default apiClient