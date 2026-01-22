const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');

// 1. Cấu hình nơi lưu file
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        // Lưu vào thư mục 'uploads/' (Bạn nhớ tạo folder này ở thư mục gốc của server nhé)
        cb(null, 'uploads/');
    },
    filename: (req, file, cb) => {
        // Đặt tên file = thời gian hiện tại + đuôi file gốc (ví dụ: 1782323.jpg) để tránh trùng tên
        cb(null, Date.now() + path.extname(file.originalname));
    }
});

// Khởi tạo biến upload
const upload = multer({ storage: storage });

// 2. API nhận file từ Android (Client gửi lên với key là 'file')
router.post('/', upload.single('file'), (req, res) => {
    // Kiểm tra xem có file gửi lên không
    if (!req.file) {
        return res.status(400).json({ message: 'Chưa chọn file hoặc file lỗi!' });
    }

    // Trả về đường dẫn file và tên file gốc cho Client
    res.json({ 
        url: `/uploads/${req.file.filename}`, 
        fileName: req.file.originalname 
    });
});

module.exports = router;