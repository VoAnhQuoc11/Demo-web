# Hướng dẫn cài đặt & chạy

## 1) Yêu cầu
- Node.js 18+ và npm.
- MySQL/MariaDB nếu bạn muốn chạy backend thực tế (schema có trong `database/schema.sql`).
- (Tuỳ chọn) Postman để import collection ở `postman/Structure_Vue3_Project_2025.postman_collection.json`.

## 2) Cấu hình biến môi trường
Sao chép `env.example` thành `.env` (hoặc `.env.local`) rồi điều chỉnh:
```
VITE_APP_NAME=Structure Vue3 Project 2025
VITE_APP_VERSION=1.0.0
VITE_API_BASE_URL=http://localhost:8000/api
VITE_API_TIMEOUT=30000
VITE_PUBLIC_ENDPOINTS=/auth/login,/auth/register,/auth/refresh
VITE_PAGE_SIZE=10
VITE_UPLOAD_MAX_SIZE=5242880
```

## 3) Cài dependencies
```bash
npm install
```

## 4) Chạy frontend (Vite)
```bash
npm run dev
```
Mặc định Vite chạy ở `http://localhost:5173`.

## 5) Backend / API
- Dự án này là frontend. Bạn cần một API backend có các endpoint:
  - Auth (public): `POST /auth/login`, `POST /auth/register`, `POST /auth/refresh`
  - Auth (private): `GET /auth/me`, `POST /auth/logout`
  - Blog (private): `GET /blogs`, `GET /blogs/:id`, `POST /blogs`, `POST /blogs/:id` với `_method=PUT`, `DELETE /blogs/:id`, `POST /blogs/upload`
- Database mẫu nằm ở `database/schema.sql` (MySQL). Import file này vào DB của bạn và trỏ `VITE_API_BASE_URL` tới backend tương ứng.
- Nếu chưa có backend, bạn có thể dùng mock API (ví dụ: json-server hoặc MSW) nhưng cần bắt chước format response:
  - Thành công: `{ success: true, data: ..., message: "...", meta?: {...} }`
  - Lỗi: `{ success: false, error: {...}, message: "..." }`

## 6) Postman
- Import file collection trong thư mục `postman/`.
- Cập nhật biến môi trường `base_url` (nếu có) cho trùng với `VITE_API_BASE_URL`.

## 7) Ảnh sản phẩm & sơ đồ cấu trúc
- Sau khi chạy `npm run dev`, mở trình duyệt, chụp màn hình các trang chính (Home, Login/Register, Blog list/detail/create/edit) để nộp kèm.
- Có thể dùng công cụ vẽ (diagrams.net / Excalidraw) để tạo sơ đồ cấu trúc thư mục hoặc luồng auth/blog, lưu vào `docs/` (tự tạo thư mục) và xuất ảnh.

## 8) Kiểm thử nhanh
- Auth: đăng ký, đăng nhập, tự động refresh token, logout.
- Blog: tạo, xem list (search + sort + paginate), xem chi tiết, cập nhật, xoá, upload ảnh.
- Kiểm tra interceptor hoạt động: private API phải có header `Authorization: Bearer <token>`.

## 9) Build
```bash
npm run build
```

