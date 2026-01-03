import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

/**
 * Composable for authentication
 */
export function useAuth() {
  const router = useRouter()
  const authStore = useAuthStore()

  const user = computed(() => authStore.user)
  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const isLoading = computed(() => authStore.isLoading)

  const login = async (credentials) => {
    const result = await authStore.login(credentials)
    if (result.success) {
      const redirect = router.currentRoute.value.query.redirect || '/'
      router.push(redirect)
    }
    return result
  }

  const register = async (userData) => {
    const result = await authStore.register(userData)
    if (result.success) {
      router.push('/')
    }
    return result
  }

  const logout = async () => {
    await authStore.logout()
    router.push('/login')
  }

  return {
    user,
    isAuthenticated,
    isLoading,
    login,
    register,
    logout
  }
}

