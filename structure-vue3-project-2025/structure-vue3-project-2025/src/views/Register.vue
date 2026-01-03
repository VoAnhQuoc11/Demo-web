<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900 mb-6 text-center">Create Account</h2>
    
    <form @submit.prevent="handleSubmit(onSubmit)" class="space-y-4">
      <Input
        v-model="formData.name"
        label="Full Name"
        type="text"
        placeholder="Enter your full name"
        required
        :error="errors.name"
      />
      
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
      
      <Input
        v-model="formData.password_confirmation"
        label="Confirm Password"
        type="password"
        placeholder="Confirm your password"
        required
        :error="errors.password_confirmation"
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
        Create Account
      </Button>
    </form>
    
    <p class="mt-6 text-center text-gray-600">
      Already have an account?
      <router-link to="/login" class="text-primary-600 font-semibold hover:underline">
        Sign in
      </router-link>
    </p>
  </div>
</template>

<script setup>
import { useForm } from '@/composables/useForm'
import { useAuth } from '@/composables/useAuth'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'

const { register } = useAuth()
const { formData, errors, isSubmitting, handleSubmit } = useForm({
  name: '',
  email: '',
  password: '',
  password_confirmation: ''
})

const onSubmit = async () => {
  if (formData.password !== formData.password_confirmation) {
    errors.password_confirmation = 'Passwords do not match'
    return
  }
  
  const result = await register(formData)
  if (!result.success) {
    errors.general = result.message
  }
}
</script>

