import { createRouter, createWebHistory } from 'vue-router'
import authRoutes from './auth'
import mainRoutes from './routes'

const routes = [
  ...authRoutes,
  ...mainRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      layout: 'DefaultLayout'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// Global navigation guard
router.beforeEach((to, from, next) => {
  // Set page title if available
  if (to.meta.title) {
    document.title = `${to.meta.title} - ${import.meta.env.VITE_APP_NAME}`
  } else {
    document.title = import.meta.env.VITE_APP_NAME
  }
  
  next()
})

export default router

