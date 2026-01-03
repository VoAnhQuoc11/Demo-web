<template>
  <div class="text-center py-12">
    <h1 class="text-6xl font-bold text-gray-900 mb-4">404</h1>
    <p class="text-xl text-gray-600 mb-8">Page not found</p>
    <router-link to="/">
      <Button>Go Home</Button>
    </router-link>
  </div>
</template>

<script setup>
import Button from '@/components/ui/Button.vue'
</script>

