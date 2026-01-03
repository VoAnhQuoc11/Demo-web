import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authAPI } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const profile = ref(null)
  const isLoading = ref(false)
  const error = ref(null)

  // Actions
  const fetchProfile = async () => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await authAPI.getMe()
      
      if (response.success) {
        profile.value = response.data.user || response.data
        return { success: true, data: profile.value }
      } else {
        error.value = response.message
        return { success: false, message: response.message }
      }
    } catch (err) {
      error.value = err.message
      return { success: false, message: err.message }
    } finally {
      isLoading.value = false
    }
  }

  const updateProfile = async (userData) => {
    isLoading.value = true
    error.value = null
    
    try {
      // This would call an update profile API endpoint
      // For now, just update local state
      profile.value = { ...profile.value, ...userData }
      return { success: true, data: profile.value }
    } catch (err) {
      error.value = err.message
      return { success: false, message: err.message }
    } finally {
      isLoading.value = false
    }
  }

  return {
    // State
    profile,
    isLoading,
    error,
    // Actions
    fetchProfile,
    updateProfile
  }
})

