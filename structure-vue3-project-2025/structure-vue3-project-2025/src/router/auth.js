/**
 * Authentication Routes
 */
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import { guestGuard } from './guards'

export default [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      requiresAuth: false,
      layout: 'AuthLayout'
    },
    beforeEnter: guestGuard
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      requiresAuth: false,
      layout: 'AuthLayout'
    },
    beforeEnter: guestGuard
  }
]

