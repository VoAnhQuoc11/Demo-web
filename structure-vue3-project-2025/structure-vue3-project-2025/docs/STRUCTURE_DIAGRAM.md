# Sơ đồ cấu trúc dự án (dạng cây)

```
structure-vue3-project-2025
├── src
│   ├── api/              # axiosInstance + service (auth, blog)
│   ├── assets/           # fonts, icons, images
│   ├── components/
│   │   ├── layout/       # Header, Footer
│   │   └── ui/           # Button, Input, Modal
│   ├── composables/      # useAuth, useForm
│   ├── config/           # constants.js
│   ├── directives/
│   ├── layouts/          # AuthLayout, DefaultLayout
│   ├── router/           # routes, guards
│   ├── store/            # Pinia stores
│   ├── styles/           # global styles, tailwind
│   ├── utils/            # apiResponse helpers
│   ├── views/            # Home, Auth, Blog CRUD
│   ├── App.vue
│   ├── main.js
│   └── i18n.js
├── database/             # schema.sql
├── postman/              # Postman collection
├── docs/                 # tài liệu, sơ đồ, ảnh chụp
├── public/
├── env.example
├── README.md
└── ...
```

> Có thể xuất sơ đồ hình ảnh bằng diagrams.net/Excalidraw và lưu vào `docs/` (PNG/SVG) nếu cần nộp kèm.

