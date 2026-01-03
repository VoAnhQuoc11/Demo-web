<template>
  <footer class="bg-gray-800 text-white mt-auto py-8">
    <div class="container mx-auto px-4 text-center">
      <p>&copy; {{ currentYear }} {{ APP_CONFIG.NAME }}. All rights reserved.</p>
      <p class="text-gray-400 text-sm mt-2">Version {{ APP_CONFIG.VERSION }}</p>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { APP_CONFIG } from '@/config/constants'

const currentYear = computed(() => new Date().getFullYear())
</script>

