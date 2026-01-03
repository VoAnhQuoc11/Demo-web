<template>
  <div class="max-w-2xl mx-auto">
    <div class="bg-white rounded-lg shadow-md p-6 mb-6">
      <h1 class="text-2xl font-bold text-gray-900 mb-6">Profile</h1>
      
      <div v-if="user" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
          <div class="px-4 py-2 bg-gray-50 rounded-lg border border-gray-300">
            {{ user.name }}
          </div>
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <div class="px-4 py-2 bg-gray-50 rounded-lg border border-gray-300">
            {{ user.email }}
          </div>
        </div>
        
        <div v-if="user.created_at">
          <label class="block text-sm font-medium text-gray-700 mb-1">Member Since</label>
          <div class="px-4 py-2 bg-gray-50 rounded-lg border border-gray-300">
            {{ new Date(user.created_at).toLocaleDateString() }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuth } from '@/composables/useAuth'

const { user } = useAuth()
</script>

