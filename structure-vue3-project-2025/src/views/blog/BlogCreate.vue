<template>
  <div class="max-w-4xl mx-auto">
    <h1 class="text-3xl font-bold text-gray-900 mb-6">Create New Blog</h1>
    
    <form @submit.prevent="handleSubmit(onSubmit)" class="bg-white rounded-lg shadow-md p-6">
      <Input
        v-model="formData.title"
        label="Title"
        type="text"
        placeholder="Enter blog title"
        required
        :error="errors.title"
      />
      
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">
          Content
          <span class="text-red-500">*</span>
        </label>
        <textarea
          v-model="formData.content"
          rows="10"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:border-primary-500 focus:ring-primary-500"
          placeholder="Write your blog content here..."
          required
        ></textarea>
        <p v-if="errors.content" class="mt-1 text-sm text-red-600">{{ errors.content }}</p>
      </div>
      
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">Image</label>
        <input
          type="file"
          accept="image/*"
          @change="handleImageChange"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:border-primary-500 focus:ring-primary-500"
        />
        <p v-if="selectedImage" class="mt-2 text-sm text-gray-600">
          Selected: {{ selectedImage.name }}
        </p>
        <p v-if="imagePreview" class="mt-2">
          <img :src="imagePreview" alt="Preview" class="max-w-xs rounded-lg" />
        </p>
      </div>
      
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">Tags (comma separated)</label>
        <Input
          v-model="tagsInput"
          type="text"
          placeholder="e.g., vue, javascript, tutorial"
        />
      </div>
      
      <div v-if="errors.general" class="mb-4 text-red-600 text-sm">
        {{ errors.general }}
      </div>
      
      <div class="flex space-x-4">
        <Button type="submit" :loading="isSubmitting" :disabled="isSubmitting">
          Create Blog
        </Button>
        <router-link to="/blogs">
          <Button type="button" variant="outline">Cancel</Button>
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useForm } from '@/composables/useForm'
import { blogAPI } from '@/api/blog'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'

const router = useRouter()
const { formData, errors, isSubmitting, handleSubmit } = useForm({
  title: '',
  content: '',
  image: null,
  tags: []
})

const tagsInput = ref('')
const selectedImage = ref(null)
const imagePreview = ref(null)

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedImage.value = file
    formData.image = file
    
    // Create preview
    const reader = new FileReader()
    reader.onload = (e) => {
      imagePreview.value = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const onSubmit = async () => {
  // Parse tags
  if (tagsInput.value) {
    formData.tags = tagsInput.value.split(',').map(tag => tag.trim()).filter(tag => tag)
  }
  
  const response = await blogAPI.create(formData)
  
  if (response.success) {
    router.push('/blogs')
  } else {
    errors.general = response.message
  }
}
</script>

