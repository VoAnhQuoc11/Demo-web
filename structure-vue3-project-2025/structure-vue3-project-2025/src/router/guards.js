/**
 * Navigation Guards
 */

export const authGuard = (to, from, next) => {
  const token = localStorage.getItem('access_token')
  
  if (!token) {
    // No token, redirect to login
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    // Token exists, allow access
    next()
  }
}

export const guestGuard = (to, from, next) => {
  const token = localStorage.getItem('access_token')
  
  if (token) {
    // Already logged in, redirect to home
    next({ name: 'Home' })
  } else {
    // No token, allow access to auth pages
    next()
  }
}

