/**
 * Main Application Routes
 */
import Home from '@/views/Home.vue'
import Profile from '@/views/Profile.vue'
import BlogList from '@/views/blog/BlogList.vue'
import BlogCreate from '@/views/blog/BlogCreate.vue'
import BlogEdit from '@/views/blog/BlogEdit.vue'
import BlogDetail from '@/views/blog/BlogDetail.vue'
import { authGuard } from './guards'

export default [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      requiresAuth: true,
      layout: 'DefaultLayout'
    },
    beforeEnter: authGuard
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: {
      requiresAuth: true,
      layout: 'DefaultLayout'
    },
    beforeEnter: authGuard
  },
  {
    path: '/blogs',
    name: 'BlogList',
    component: BlogList,
    meta: {
      requiresAuth: true,
      layout: 'DefaultLayout'
    },
    beforeEnter: authGuard
  },
  {
    path: '/blogs/create',
    name: 'BlogCreate',
    component: BlogCreate,
    meta: {
      requiresAuth: true,
      layout: 'DefaultLayout'
    },
    beforeEnter: authGuard
  },
  {
    path: '/blogs/:id',
    name: 'BlogDetail',
    component: BlogDetail,
    meta: {
      requiresAuth: true,
      layout: 'DefaultLayout'
    },
    beforeEnter: authGuard
  },
  {
    path: '/blogs/:id/edit',
    name: 'BlogEdit',
    component: BlogEdit,
    meta: {
      requiresAuth: true,
      layout: 'DefaultLayout'
    },
    beforeEnter: authGuard
  }
]

