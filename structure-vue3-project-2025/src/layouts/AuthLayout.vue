<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-2xl shadow-xl p-8">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">{{ APP_CONFIG.NAME }}</h1>
          <p class="text-gray-600">Welcome back!</p>
        </div>
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { APP_CONFIG } from '@/config/constants'
</script>

