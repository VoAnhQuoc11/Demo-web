import { ref, reactive } from 'vue'

/**
 * Composable for form handling
 */
export function useForm(initialValues = {}) {
  const formData = reactive({ ...initialValues })
  const errors = reactive({})
  const isSubmitting = ref(false)

  const setField = (field, value) => {
    formData[field] = value
    if (errors[field]) {
      delete errors[field]
    }
  }

  const setError = (field, message) => {
    errors[field] = message
  }

  const setErrors = (errorObject) => {
    Object.keys(errorObject).forEach(key => {
      errors[key] = errorObject[key]
    })
  }

  const clearErrors = () => {
    Object.keys(errors).forEach(key => {
      delete errors[key]
    })
  }

  const reset = () => {
    Object.keys(formData).forEach(key => {
      formData[key] = initialValues[key] || ''
    })
    clearErrors()
    isSubmitting.value = false
  }

  const handleSubmit = async (submitFn) => {
    isSubmitting.value = true
    clearErrors()
    
    try {
      const result = await submitFn(formData)
      return result
    } catch (error) {
      if (error.response?.data?.errors) {
        setErrors(error.response.data.errors)
      } else {
        setError('general', error.message || 'An error occurred')
      }
      throw error
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    formData,
    errors,
    isSubmitting,
    setField,
    setError,
    setErrors,
    clearErrors,
    reset,
    handleSubmit
  }
}

