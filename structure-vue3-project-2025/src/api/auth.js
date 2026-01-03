import axiosInstance from './axiosInstance'
import { successResponse, errorResponse } from '../utils/apiResponse'

/**
 * Standard API Response Structure:
 * {
 *   success: boolean,
 *   data: any,
 *   message: string,
 *   meta?: {
 *     total?: number,
 *     page?: number,
 *     limit?: number,
 *     ...
 *   }
 * }
 */

/**
 * Standard API Request Structure:
 * {
 *   ...requestData
 * }
 */

// Auth API Service
export const authAPI = {
  /**
   * Login
   * POST /auth/login
   * Request: { email: string, password: string }
   * Response: { success: true, data: { user: {...}, access_token: string, refresh_token: string }, message: "..." }
   */
  login: async (credentials) => {
    try {
      const response = await axiosInstance.post('/auth/login', credentials)
      return successResponse(response, 'Login successful')
    } catch (error) {
      return errorResponse(error, 'Login failed')
    }
  },

  /**
   * Register
   * POST /auth/register
   * Request: { name: string, email: string, password: string, password_confirmation: string }
   * Response: { success: true, data: { user: {...}, access_token: string, refresh_token: string }, message: "..." }
   */
  register: async (userData) => {
    try {
      const response = await axiosInstance.post('/auth/register', userData)
      return successResponse(response, 'Registration successful')
    } catch (error) {
      return errorResponse(error, 'Registration failed')
    }
  },

  /**
   * Logout
   * POST /auth/logout
   * Request: {} (token in header)
   * Response: { success: true, message: "..." }
   */
  logout: async () => {
    try {
      const response = await axiosInstance.post('/auth/logout')
      return successResponse(response, 'Logout successful')
    } catch (error) {
      return errorResponse(error, 'Logout failed')
    }
  },

  /**
   * Get current user
   * GET /auth/me
   * Request: {} (token in header)
   * Response: { success: true, data: { user: {...} }, message: "..." }
   */
  getMe: async () => {
    try {
      const response = await axiosInstance.get('/auth/me')
      return successResponse(response, 'User retrieved successfully')
    } catch (error) {
      return errorResponse(error, 'Failed to get user')
    }
  },

  /**
   * Refresh token
   * POST /auth/refresh
   * Request: { refresh_token: string }
   * Response: { success: true, data: { access_token: string }, message: "..." }
   */
  refreshToken: async (refreshToken) => {
    try {
      const response = await axiosInstance.post('/auth/refresh', {
        refresh_token: refreshToken
      })
      return successResponse(response, 'Token refreshed successfully')
    } catch (error) {
      return errorResponse(error, 'Token refresh failed')
    }
  }
}

