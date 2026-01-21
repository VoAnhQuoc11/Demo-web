const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');

// Cấu hình lưu file
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/'); // File sẽ lưu vào thư mục 'uploads'
    },
    filename: (req, file, cb) => {
        // Đặt tên file là: timestamp + đuôi mở rộng (để tránh trùng)
        cb(null, Date.now() + path.extname(file.originalname));
    }
});

const upload = multer({ storage: storage });

// API nhận file
router.post('/', upload.single('file'), (req, res) => {
    if (!req.file) {
        return res.status(400).json({ message: 'Chưa chọn file!' });
    }
    // Trả về đường dẫn file và tên file gốc
    res.json({ 
        url: `/uploads/${req.file.filename}`, 
        fileName: req.file.originalname 
    });
});

module.exports = router;