<template>
  <div id="app">
    <component :is="layout">
      <router-view />
    </component>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'

const route = useRoute()

const layout = computed(() => {
  const layoutName = route.meta.layout || 'DefaultLayout'
  return layoutName === 'AuthLayout' ? AuthLayout : DefaultLayout
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style>

