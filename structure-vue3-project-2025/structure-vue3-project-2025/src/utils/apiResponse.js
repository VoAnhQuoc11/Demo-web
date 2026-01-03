// Helper functions to standardize API responses across services

export const successResponse = (response, fallbackMessage = 'Success') => ({
  success: true,
  data: response?.data?.data ?? response?.data ?? null,
  message: response?.data?.message ?? fallbackMessage,
  meta: response?.data?.meta
})

export const errorResponse = (error, fallbackMessage = 'Request failed') => ({
  success: false,
  error: error?.response?.data ?? error?.message ?? null,
  message: error?.response?.data?.message ?? fallbackMessage
})

// Utility to unwrap data directly if service needs raw data
export const extractData = (resp) => resp?.data?.data ?? resp?.data ?? null

