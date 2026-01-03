<template>
  <header class="bg-white shadow-md">
    <nav class="container mx-auto px-4 py-4">
      <div class="flex items-center justify-between">
        <router-link to="/" class="text-2xl font-bold text-primary-600">
          {{ APP_CONFIG.NAME }}
        </router-link>
        
        <div class="flex items-center space-x-4">
          <router-link
            v-for="item in navigation"
            :key="item.name"
            :to="item.path"
            class="px-4 py-2 text-gray-700 hover:text-primary-600 transition-colors"
            active-class="text-primary-600 font-semibold"
          >
            {{ item.name }}
          </router-link>
          
          <div class="flex items-center space-x-2">
            <span class="text-gray-700">{{ user?.name || 'User' }}</span>
            <button
              @click="handleLogout"
              class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { APP_CONFIG } from '@/config/constants'

const router = useRouter()
const { user, logout } = useAuth()

const navigation = [
  { name: 'Home', path: '/' },
  { name: 'Blogs', path: '/blogs' },
  { name: 'Profile', path: '/profile' }
]

const handleLogout = async () => {
  await logout()
}
</script>

