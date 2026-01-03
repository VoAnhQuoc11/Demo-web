<template>
  <div class="max-w-4xl mx-auto">
    <div v-if="loading" class="text-center py-12">
      <div class="text-gray-500">Loading blog...</div>
    </div>
    
    <div v-else-if="blog" class="bg-white rounded-lg shadow-md overflow-hidden">
      <img
        v-if="blog.image"
        :src="blog.image"
        :alt="blog.title"
        class="w-full h-64 object-cover"
      />
      
      <div class="p-8">
        <div class="flex justify-between items-start mb-4">
          <h1 class="text-4xl font-bold text-gray-900">{{ blog.title }}</h1>
          <div class="flex space-x-2">
            <router-link :to="`/blogs/${blog.id}/edit`">
              <Button variant="outline" size="sm">Edit</Button>
            </router-link>
            <Button variant="danger" size="sm" @click="handleDelete">Delete</Button>
          </div>
        </div>
        
        <div class="flex items-center space-x-4 text-gray-600 mb-6">
          <span>{{ new Date(blog.created_at).toLocaleDateString() }}</span>
          <span v-if="blog.author">By {{ blog.author.name }}</span>
        </div>
        
        <div v-if="blog.tags && blog.tags.length > 0" class="flex flex-wrap gap-2 mb-6">
          <span
            v-for="tag in blog.tags"
            :key="tag"
            class="px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-sm"
          >
            {{ tag }}
          </span>
        </div>
        
        <div class="prose max-w-none">
          <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">{{ blog.content }}</p>
        </div>
      </div>
    </div>
    
    <div v-else class="text-center py-12">
      <div class="text-gray-500 mb-4">Blog not found</div>
      <router-link to="/blogs">
        <Button>Back to Blogs</Button>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { blogAPI } from '@/api/blog'
import Button from '@/components/ui/Button.vue'

const route = useRoute()
const router = useRouter()
const blogId = route.params.id
const blog = ref(null)
const loading = ref(true)

const loadBlog = async () => {
  loading.value = true
  try {
    const response = await blogAPI.getById(blogId)
    if (response.success && response.data) {
      blog.value = response.data.blog || response.data
    }
  } catch (error) {
    console.error('Failed to load blog:', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = async () => {
  if (confirm('Are you sure you want to delete this blog?')) {
    try {
      const response = await blogAPI.delete(blogId)
      if (response.success) {
        router.push('/blogs')
      }
    } catch (error) {
      console.error('Failed to delete blog:', error)
    }
  }
}

onMounted(() => {
  loadBlog()
})
</script>

