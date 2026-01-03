import { createI18n } from 'vue-i18n'

// Example i18n configuration
// Install: npm install vue-i18n

const messages = {
  en: {
    welcome: 'Welcome',
    login: 'Login',
    register: 'Register',
    logout: 'Logout',
    home: 'Home',
    blogs: 'Blogs',
    profile: 'Profile'
  },
  vi: {
    welcome: 'Chào mừng',
    login: 'Đăng nhập',
    register: 'Đăng ký',
    logout: 'Đăng xuất',
    home: 'Trang chủ',
    blogs: 'Bài viết',
    profile: 'Hồ sơ'
  }
}

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  fallbackLocale: 'en',
  messages
})

export default i18n

