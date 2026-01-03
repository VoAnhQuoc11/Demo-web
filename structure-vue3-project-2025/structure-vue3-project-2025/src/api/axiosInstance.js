import axios from 'axios'

// Global variables from environment
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api'
const API_TIMEOUT = import.meta.env.VITE_API_TIMEOUT || 30000
const PUBLIC_ENDPOINTS = (import.meta.env.VITE_PUBLIC_ENDPOINTS || '/auth/login,/auth/register,/auth/refresh').split(',')

// Create axios instance
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// Request interceptor - Add token for private APIs
axiosInstance.interceptors.request.use(
  (config) => {
    // Check if endpoint is public
    const isPublicEndpoint = PUBLIC_ENDPOINTS.some(endpoint => 
      config.url.includes(endpoint.trim())
    )

    // Add token for private endpoints
    if (!isPublicEndpoint) {
      const token = localStorage.getItem('access_token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }

    // Add request timestamp
    config.metadata = { startTime: new Date() }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - Handle standard response structure
axiosInstance.interceptors.response.use(
  (response) => {
    // Calculate request duration
    if (response.config.metadata) {
      const duration = new Date() - response.config.metadata.startTime
      console.log(`API Request ${response.config.url} took ${duration}ms`)
    }

    // Standard response structure
    // { success: true, data: {...}, message: "...", meta: {...} }
    return response
  },
  async (error) => {
    const originalRequest = error.config

    // Handle 401 Unauthorized - Refresh token logic
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = localStorage.getItem('refresh_token')
        if (refreshToken) {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refresh_token: refreshToken
          })

          const { access_token } = response.data.data
          localStorage.setItem('access_token', access_token)
          originalRequest.headers.Authorization = `Bearer ${access_token}`

          return axiosInstance(originalRequest)
        }
      } catch (refreshError) {
        // Refresh failed, logout user
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    // Standard error response structure
    // { success: false, error: {...}, message: "..." }
    return Promise.reject(error)
  }
)

export default axiosInstance

// Export global configuration
export const API_CONFIG = {
  BASE_URL: API_BASE_URL,
  TIMEOUT: API_TIMEOUT,
  PUBLIC_ENDPOINTS
}

