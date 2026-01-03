<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900 mb-6 text-center">Sign In</h2>
    
    <form @submit.prevent="handleSubmit(onSubmit)" class="space-y-4">
      <Input
        v-model="formData.email"
        label="Email"
        type="email"
        placeholder="Enter your email"
        required
        :error="errors.email"
      />
      
      <Input
        v-model="formData.password"
        label="Password"
        type="password"
        placeholder="Enter your password"
        required
        :error="errors.password"
      />
      
      <div v-if="errors.general" class="text-red-600 text-sm text-center">
        {{ errors.general }}
      </div>
      
      <Button
        type="submit"
        :loading="isSubmitting"
        :disabled="isSubmitting"
        class="w-full"
        size="lg"
      >
        Sign In
      </Button>
    </form>
    
    <p class="mt-6 text-center text-gray-600">
      Don't have an account?
      <router-link to="/register" class="text-primary-600 font-semibold hover:underline">
        Sign up
      </router-link>
    </p>
  </div>
</template>

<script setup>
import { useForm } from '@/composables/useForm'
import { useAuth } from '@/composables/useAuth'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'

const { login } = useAuth()
const { formData, errors, isSubmitting, handleSubmit } = useForm({
  email: '',
  password: ''
})

const onSubmit = async () => {
  const result = await login(formData)
  if (!result.success) {
    errors.general = result.message
  }
}
</script>

