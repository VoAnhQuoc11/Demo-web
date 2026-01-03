import axiosInstance from './axiosInstance'
import { successResponse, errorResponse } from '../utils/apiResponse'

/**
 * Blog API Service
 * All endpoints require authentication (private API)
 */

export const blogAPI = {
  /**
   * Get all blogs with pagination, search, and sort
   * GET /blogs?page=1&limit=10&search=keyword&sort=created_at&order=desc
   * Request: { page?: number, limit?: number, search?: string, sort?: string, order?: 'asc'|'desc' }
   * Response: { success: true, data: { blogs: [...], pagination: {...} }, message: "...", meta: {...} }
   */
  getAll: async (params = {}) => {
    try {
      const queryParams = new URLSearchParams()

      if (params.page) queryParams.append('page', params.page)
      if (params.limit) queryParams.append('limit', params.limit)
      if (params.search) queryParams.append('search', params.search)
      if (params.sort) queryParams.append('sort', params.sort)
      if (params.order) queryParams.append('order', params.order)

      const queryString = queryParams.toString()
      const url = `/blogs${queryString ? `?${queryString}` : ''}`

      const response = await axiosInstance.get(url)
      return {
        ...successResponse(response, 'Blogs retrieved successfully'),
        meta: response.data.meta || {}
      }
    } catch (error) {
      return errorResponse(error, 'Failed to get blogs')
    }
  },

  /**
   * Get single blog by ID
   * GET /blogs/:id
   * Request: {} (id in URL)
   * Response: { success: true, data: { blog: {...} }, message: "..." }
   */
  getById: async (id) => {
    try {
      const response = await axiosInstance.get(`/blogs/${id}`)
      return successResponse(response, 'Blog retrieved successfully')
    } catch (error) {
      return errorResponse(error, 'Failed to get blog')
    }
  },

  /**
   * Create new blog
   * POST /blogs
   * Request: { title: string, content: string, image?: File, tags?: string[] }
   * Response: { success: true, data: { blog: {...} }, message: "..." }
   */
  create: async (blogData) => {
    try {
      // Handle file upload with FormData
      const formData = new FormData()
      formData.append('title', blogData.title)
      formData.append('content', blogData.content)

      if (blogData.image) {
        formData.append('image', blogData.image)
      }

      if (blogData.tags && Array.isArray(blogData.tags)) {
        blogData.tags.forEach(tag => formData.append('tags[]', tag))
      }

      const response = await axiosInstance.post('/blogs', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      return successResponse(response, 'Blog created successfully')
    } catch (error) {
      return errorResponse(error, 'Failed to create blog')
    }
  },

  /**
   * Update blog
   * PUT /blogs/:id
   * Request: { title?: string, content?: string, image?: File, tags?: string[] }
   * Response: { success: true, data: { blog: {...} }, message: "..." }
   */
  update: async (id, blogData) => {
    try {
      // Handle file upload with FormData
      const formData = new FormData()

      if (blogData.title) formData.append('title', blogData.title)
      if (blogData.content) formData.append('content', blogData.content)
      if (blogData.image) formData.append('image', blogData.image)
      if (blogData.tags && Array.isArray(blogData.tags)) {
        blogData.tags.forEach(tag => formData.append('tags[]', tag))
      }
      // For PUT requests, some backends require _method
      formData.append('_method', 'PUT')

      const response = await axiosInstance.post(`/blogs/${id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      return successResponse(response, 'Blog updated successfully')
    } catch (error) {
      return errorResponse(error, 'Failed to update blog')
    }
  },

  /**
   * Delete blog
   * DELETE /blogs/:id
   * Request: {} (id in URL)
   * Response: { success: true, message: "..." }
   */
  delete: async (id) => {
    try {
      const response = await axiosInstance.delete(`/blogs/${id}`)
      return successResponse(response, 'Blog deleted successfully')
    } catch (error) {
      return errorResponse(error, 'Failed to delete blog')
    }
  },

  /**
   * Upload image
   * POST /blogs/upload
   * Request: FormData with image file
   * Response: { success: true, data: { url: string, path: string }, message: "..." }
   */
  uploadImage: async (file) => {
    try {
      const formData = new FormData()
      formData.append('image', file)

      const response = await axiosInstance.post('/blogs/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      return successResponse(response, 'Image uploaded successfully')
    } catch (error) {
      return errorResponse(error, 'Failed to upload image')
    }
  }
}

