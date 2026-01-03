<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold text-gray-900">Blogs</h1>
      <router-link to="/blogs/create">
        <Button>Create New Blog</Button>
      </router-link>
    </div>

    <!-- Search and Filter -->
    <div class="bg-white rounded-lg shadow-md p-4 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Input
          v-model="searchQuery"
          placeholder="Search blogs..."
          @input="handleSearch"
        />
        <select
          v-model="sortBy"
          @change="loadBlogs"
          class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="created_at">Date</option>
          <option value="title">Title</option>
          <option value="updated_at">Last Updated</option>
        </select>
        <select
          v-model="sortOrder"
          @change="loadBlogs"
          class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="desc">Descending</option>
          <option value="asc">Ascending</option>
        </select>
        <Button @click="loadBlogs" :loading="loading">Refresh</Button>
      </div>
    </div>

    <!-- Blog List -->
    <div v-if="loading && blogs.length === 0" class="text-center py-12">
      <div class="text-gray-500">Loading blogs...</div>
    </div>

    <div v-else-if="blogs.length === 0" class="text-center py-12">
      <div class="text-gray-500 mb-4">No blogs found</div>
      <router-link to="/blogs/create">
        <Button>Create Your First Blog</Button>
      </router-link>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="blog in blogs"
        :key="blog.id"
        class="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
        @click="$router.push(`/blogs/${blog.id}`)"
      >
        <img
  :src="`http://localhost:8000/uploads/${blog.image}`"
  alt="Blog Image"
  @error="$event.target.src = '/default.jpg'; $event.target.onerror = null;"
/>
        <div class="p-6">
          <h3 class="text-xl font-bold text-gray-900 mb-2 line-clamp-2">{{ blog.title }}</h3>
          <p class="text-gray-600 mb-4 line-clamp-3">{{ blog.content }}</p>
          <div class="flex justify-between items-center text-sm text-gray-500">
            <span>{{ new Date(blog.created_at).toLocaleDateString() }}</span>
            <div class="flex space-x-2">
              <button
                @click.stop="$router.push(`/blogs/${blog.id}/edit`)"
                class="text-primary-600 hover:text-primary-700"
              >
                Edit
              </button>
              <button
                @click.stop="handleDelete(blog.id)"
                class="text-red-600 hover:text-red-700"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="meta.total > meta.limit" class="mt-6 flex justify-center">
      <div class="flex space-x-2">
        <Button
          @click="changePage(meta.page - 1)"
          :disabled="meta.page === 1"
          variant="outline"
        >
          Previous
        </Button>
        <span class="px-4 py-2 flex items-center">
          Page {{ meta.page }} of {{ Math.ceil(meta.total / meta.limit) }}
        </span>
        <Button
          @click="changePage(meta.page + 1)"
          :disabled="meta.page >= Math.ceil(meta.total / meta.limit)"
          variant="outline"
        >
          Next
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { blogAPI } from '@/api/blog'
import { APP_CONFIG } from '@/config/constants'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'

const router = useRouter()
const blogs = ref([])
const loading = ref(false)
const searchQuery = ref('')
const sortBy = ref('created_at')
const sortOrder = ref('desc')
const meta = ref({
  page: 1,
  limit: APP_CONFIG.PAGE_SIZE,
  total: 0
})

let searchTimeout = null

const loadBlogs = async () => {
  loading.value = true
  try {
    const response = await blogAPI.getAll({
      page: meta.value.page,
      limit: meta.value.limit,
      search: searchQuery.value,
      sort: sortBy.value,
      order: sortOrder.value
    })

    if (response.success) {
      blogs.value = response.data.blogs || response.data || []
      meta.value = {
        ...meta.value,
        ...response.meta,
        total: response.meta?.total || blogs.value.length
      }
    }
  } catch (error) {
    console.error('Failed to load blogs:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    meta.value.page = 1
    loadBlogs()
  }, 500)
}

const changePage = (page) => {
  meta.value.page = page
  loadBlogs()
}

const handleDelete = async (id) => {
  if (confirm('Are you sure you want to delete this blog?')) {
    try {
      const response = await blogAPI.delete(id)
      if (response.success) {
        loadBlogs()
      }
    } catch (error) {
      console.error('Failed to delete blog:', error)
    }
  }
}

onMounted(() => {
  loadBlogs()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

