<template>
  <div>
    <div class="bg-gradient-to-r from-primary-600 to-indigo-600 rounded-lg p-8 text-white mb-8">
      <h1 class="text-4xl font-bold mb-2">Welcome, {{ user?.name || 'User' }}!</h1>
      <p class="text-lg opacity-90">Manage your blogs and stay organized</p>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="text-3xl font-bold text-primary-600 mb-2">{{ stats.totalBlogs }}</div>
        <div class="text-gray-600">Total Blogs</div>
      </div>
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="text-3xl font-bold text-green-600 mb-2">{{ stats.publishedBlogs }}</div>
        <div class="text-gray-600">Published</div>
      </div>
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="text-3xl font-bold text-blue-600 mb-2">{{ stats.drafts }}</div>
        <div class="text-gray-600">Drafts</div>
      </div>
    </div>
    
    <div class="bg-white rounded-lg shadow-md p-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-2xl font-bold text-gray-900">Quick Actions</h2>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <router-link
          to="/blogs/create"
          class="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors text-center"
        >
          <div class="text-4xl mb-2">📝</div>
          <div class="font-semibold text-gray-900">Create New Blog</div>
          <div class="text-sm text-gray-600">Start writing your next post</div>
        </router-link>
        <router-link
          to="/blogs"
          class="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors text-center"
        >
          <div class="text-4xl mb-2">📚</div>
          <div class="font-semibold text-gray-900">View All Blogs</div>
          <div class="text-sm text-gray-600">Manage your blog posts</div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { blogAPI } from '@/api/blog'

const { user } = useAuth()
const stats = ref({
  totalBlogs: 0,
  publishedBlogs: 0,
  drafts: 0
})

onMounted(async () => {
  try {
    const response = await blogAPI.getAll()
    if (response.success && response.data) {
      stats.value.totalBlogs = response.data.blogs?.length || 0
      stats.value.publishedBlogs = response.data.blogs?.filter(b => b.status === 'published').length || 0
      stats.value.drafts = response.data.blogs?.filter(b => b.status === 'draft').length || 0
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
})
</script>

