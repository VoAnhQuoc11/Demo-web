const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');

// Cấu hình nơi lưu file
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/'); // Đảm bảo thư mục 'uploads' đã tồn tại ở root server
    },
    filename: (req, file, cb) => {
        // Đặt tên file = timestamp + tên gốc để tránh trùng
        cb(null, Date.now() + path.extname(file.originalname));
    }
});

const upload = multer({ storage: storage });

// API nhận file từ Android
router.post('/', upload.single('file'), (req, res) => {
    if (!req.file) {
        return res.status(400).json({ message: 'Không có file nào được gửi lên' });
    }
    // Trả về link file cho Android
    res.json({ 
        url: `/uploads/${req.file.filename}`, 
        fileName: req.file.originalname 
    });
});

module.exports = router;