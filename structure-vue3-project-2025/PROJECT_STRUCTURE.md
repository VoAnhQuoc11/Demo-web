# Cấu trúc dự án

```
structure-vue3-project-2025
├── src
│   ├── api/              # Axios instance + service modules (auth, blog)
│   ├── assets/           # Fonts, icons, images
│   ├── components/
│   │   ├── layout/       # Header, Footer
│   │   └── ui/           # Button, Input, Modal
│   ├── composables/      # useAuth, useForm
│   ├── config/           # constants.js (app/api constants)
│   ├── directives/       # (placeholder for custom directives)
│   ├── layouts/          # AuthLayout, DefaultLayout
│   ├── router/           # routes, guards, route groups
│   ├── store/            # Pinia stores (auth, user)
│   ├── styles/           # global styles, tailwind setup
│   ├── views/            # Pages (home, auth, profile, blog CRUD)
│   ├── App.vue
│   ├── main.js
│   ├── i18n.js
│   └── shims-vue.d.ts
├── public/               # index.html, static assets
├── database/             # schema.sql (MySQL seed)
├── postman/              # Postman collection
├── env.example           # Mẫu biến môi trường Vite
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
└── README.md
```

## Ghi chú
- API chia Public (login, register, refresh) và Private (me, logout, blog CRUD/upload/search/sort/paginate).
- Standard response dự kiến: `{ success, data, message, meta }`. Standard error: `{ success: false, error, message }`.
- Interceptor đã thêm token và refresh token tự động (xem `src/api/axiosInstance.js`).
- Các constant toàn cục đặt trong `src/config/constants.js`; env được bơm qua `import.meta.env`.

