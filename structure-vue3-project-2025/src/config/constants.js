/**
 * Global Constants
 */

// Application Constants
export const APP_CONFIG = {
  NAME: import.meta.env.VITE_APP_NAME || 'Structure Vue3 Project 2025',
  VERSION: import.meta.env.VITE_APP_VERSION || '1.0.0',
  PAGE_SIZE: parseInt(import.meta.env.VITE_PAGE_SIZE) || 10,
  UPLOAD_MAX_SIZE: parseInt(import.meta.env.VITE_UPLOAD_MAX_SIZE) || 5242880, // 5MB
}

// Storage Keys
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER: 'user',
  THEME: 'theme',
  LANGUAGE: 'language'
}

// API Endpoints
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    ME: '/auth/me',
    REFRESH: '/auth/refresh'
  },
  BLOG: {
    LIST: '/blogs',
    DETAIL: (id) => `/blogs/${id}`,
    CREATE: '/blogs',
    UPDATE: (id) => `/blogs/${id}`,
    DELETE: (id) => `/blogs/${id}`,
    UPLOAD: '/blogs/upload'
  }
}

// Status Codes
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500
}

// Blog Sort Options
export const BLOG_SORT_OPTIONS = {
  CREATED_AT: 'created_at',
  TITLE: 'title',
  UPDATED_AT: 'updated_at'
}

export const SORT_ORDER = {
  ASC: 'asc',
  DESC: 'desc'
}

