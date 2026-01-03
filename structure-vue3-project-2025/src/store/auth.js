import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/api/auth'
import { STORAGE_KEYS } from '@/config/constants'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(JSON.parse(localStorage.getItem(STORAGE_KEYS.USER) || 'null'))
  const accessToken = ref(localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN) || null)
  const refreshToken = ref(localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN) || null)
  const isLoading = ref(false)

  // Getters
  const isAuthenticated = computed(() => !!accessToken.value && !!user.value)

  // Actions
  const login = async (credentials) => {
    isLoading.value = true
    try {
      const response = await authAPI.login(credentials)
      
      if (response.success) {
        const { user: userData, access_token, refresh_token } = response.data
        
        // Store tokens and user
        accessToken.value = access_token
        refreshToken.value = refresh_token
        user.value = userData
        
        localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, access_token)
        localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refresh_token)
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userData))
        
        return { success: true, message: response.message }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: error.message || 'Login failed' }
    } finally {
      isLoading.value = false
    }
  }

  const register = async (userData) => {
    isLoading.value = true
    try {
      const response = await authAPI.register(userData)
      
      if (response.success) {
        const { user: newUser, access_token, refresh_token } = response.data
        
        accessToken.value = access_token
        refreshToken.value = refresh_token
        user.value = newUser
        
        localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, access_token)
        localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refresh_token)
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(newUser))
        
        return { success: true, message: response.message }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: error.message || 'Registration failed' }
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    isLoading.value = true
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // Clear state regardless of API call result
      accessToken.value = null
      refreshToken.value = null
      user.value = null
      
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER)
      
      isLoading.value = false
    }
  }

  const getMe = async () => {
    isLoading.value = true
    try {
      const response = await authAPI.getMe()
      
      if (response.success) {
        user.value = response.data.user || response.data
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user.value))
        return { success: true, data: user.value }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: error.message || 'Failed to get user' }
    } finally {
      isLoading.value = false
    }
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    isLoading,
    // Getters
    isAuthenticated,
    // Actions
    login,
    register,
    logout,
    getMe
  }
})

