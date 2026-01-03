const express = require('express');
const mysql = require('mysql');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const bcrypt = require('bcryptjs');
const slugify = require('slugify');

const app = express();

// --- CẤU HÌNH MIDDLEWARE ---
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static('uploads')); // Công khai thư mục ảnh

// --- KẾT NỐI DATABASE ---
const db = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "",
    database: "structure_vue3_project_2025",
    multipleStatements: true // Quan trọng để chạy Transaction
});

db.connect((err) => {
    if (err) console.error('Lỗi kết nối CSDL:', err);
    else console.log('Đã kết nối MySQL thành công!');
});

// --- CẤU HÌNH UPLOAD ẢNH (MULTER) ---
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir);

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: (req, file, cb) => cb(null, Date.now() + path.extname(file.originalname))
});
const upload = multer({ storage: storage });



app.post('/api/auth/register', (req, res) => {
    const { name, email, password } = req.body;
    if (!name || !email || !password) return res.status(400).json("Thiếu thông tin");

    const salt = bcrypt.genSaltSync(10);
    const hash = bcrypt.hashSync(password, salt);

    const sql = "INSERT INTO users (name, email, password) VALUES (?)";
    db.query(sql, [[name, email, hash]], (err, result) => {
        if (err) return res.status(500).json(err);
        return res.json({ message: "Đăng ký thành công", userId: result.insertId });
    });
});

app.post('/api/auth/login', (req, res) => {
    const { email, password } = req.body;
    const sql = "SELECT * FROM users WHERE email = ?";
    db.query(sql, [email], (err, data) => {
        if (err) return res.status(500).json(err);
        if (data.length === 0) return res.status(404).json("Email không tồn tại");

        const isPasswordValid = bcrypt.compareSync(password, data[0].password);
        if (!isPasswordValid) return res.status(401).json("Sai mật khẩu");

        const { password: hashedPassword, ...userWithoutPass } = data[0];
        return res.json({ message: "Login thành công", user: userWithoutPass });
    });
});


app.get('/api/blogs', (req, res) => {
    const searchTerm = req.query.search;
    const sortType = req.query.sort;

    let sql = `
        SELECT b.*, u.name as author_name,
        GROUP_CONCAT(t.name) as tags_list
        FROM blogs b
        LEFT JOIN users u ON b.user_id = u.id
        LEFT JOIN blog_tags bt ON b.id = bt.blog_id
        LEFT JOIN tags t ON bt.tag_id = t.id
    `;

    const params = [];

    // --- XỬ LÝ SEARCH ---
    if (searchTerm) {
        sql += ` WHERE b.title LIKE ? OR b.content LIKE ?`;
        params.push(`%${searchTerm}%`, `%${searchTerm}%`);
    }

    sql += ` GROUP BY b.id`;

    // --- XỬ LÝ SORT ---
    switch (sortType) {
        case 'oldest': sql += ` ORDER BY b.created_at ASC`; break;     // Cũ nhất trước
        case 'az':     sql += ` ORDER BY b.title ASC`; break;          // A -> Z
        case 'za':     sql += ` ORDER BY b.title DESC`; break;         // Z -> A
        default:       sql += ` ORDER BY b.created_at DESC`; break;    // Mặc định: Mới nhất trước
    }

    db.query(sql, params, (err, data) => {
        if (err) return res.status(500).json(err);
        return res.json(data);
    });
});

// Xem chi tiết 1 bài viết
app.get('/api/blogs/:id', (req, res) => {
    const sql = `SELECT * FROM blogs WHERE id = ?`;
    db.query(sql, [req.params.id], (err, data) => {
        if (err) return res.status(500).json(err);
        if (data.length === 0) return res.status(404).json("Không tìm thấy");
        return res.json(data[0]);
    });
});

/**
CREATE  - Có Upload ảnh + Xử lý Tags
 */
app.post('/api/blogs', upload.single('image'), (req, res) => {
    const { title, content, user_id, tags } = req.body;
    const image = req.file ? req.file.filename : null;
    const slug = slugify(title, { lower: true, strict: true }) + '-' + Date.now();
    const authorId = user_id || 1;

    // Bắt đầu Transaction
    db.beginTransaction((err) => {
        if (err) return res.status(500).json(err);

        // Bước 1: Tạo Blog
        const sqlBlog = "INSERT INTO blogs (user_id, title, slug, content, image, status) VALUES (?, ?, ?, ?, ?, 'published')";
        db.query(sqlBlog, [authorId, title, slug, content, image], (err, result) => {
            if (err) return db.rollback(() => res.status(500).json(err));

            const blogId = result.insertId;

            // Nếu không có tags -> Commit luôn
            if (!tags) {
                return db.commit((err) => {
                    if (err) return db.rollback(() => res.status(500).json(err));
                    res.json({ message: "Tạo thành công", blogId });
                });
            }

            // Bước 2: Xử lý Tags (Hàm phụ trợ ở dưới cùng file)
            handleTags(blogId, tags, db, res);
        });
    });
});

/**
 *  UPDATE  - Cập nhật ảnh (xóa ảnh cũ) + Cập nhật Tags
 */
app.post('/api/blogs/:id', upload.single('image'), (req, res) => {
    const blogId = req.params.id;
    const { title, content, status, tags } = req.body;
    let image = req.body.old_image; // Tên ảnh cũ (client gửi lên)

    // Nếu có upload ảnh mới -> Dùng ảnh mới
    if (req.file) {
        image = req.file.filename;
        // Xóa ảnh cũ khỏi ổ cứng để tiết kiệm dung lượng
        if (req.body.old_image) {
            const oldPath = path.join(__dirname, 'uploads', req.body.old_image);
            if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
        }
    }

    const newSlug = slugify(title, { lower: true, strict: true }) + '-' + Date.now();

    // Bắt đầu Transaction
    db.beginTransaction((err) => {
        if (err) return res.status(500).json(err);

        // Bước 1: Update thông tin Blog
        const sql = "UPDATE blogs SET title = ?, slug = ?, content = ?, image = ?, status = ? WHERE id = ?";
        db.query(sql, [title, newSlug, content, image, status || 'published', blogId], (err, result) => {
            if (err) return db.rollback(() => res.status(500).json(err));

            // Bước 2: Reset Tags (Xóa hết tags cũ của blog này rồi thêm lại tags mới)
            db.query("DELETE FROM blog_tags WHERE blog_id = ?", [blogId], (err) => {
                if (err) return db.rollback(() => res.status(500).json(err));

                if (!tags) {
                    return db.commit((err) => {
                        if (err) return db.rollback(() => res.status(500).json(err));
                        res.json({ message: "Cập nhật thành công!" });
                    });
                }
                // Thêm tags mới
                handleTags(blogId, tags, db, res);
            });
        });
    });
});

/**
  DELETE (DELETE) - Xóa bài viết + Xóa ảnh
 */
app.delete('/api/blogs/:id', (req, res) => {
    const blogId = req.params.id;

    // Bước 1: Lấy tên ảnh để xóa file
    db.query("SELECT image FROM blogs WHERE id = ?", [blogId], (err, data) => {
        if (err) return res.status(500).json(err);

        if (data.length > 0 && data[0].image) {
            const imagePath = path.join(__dirname, 'uploads', data[0].image);
            if (fs.existsSync(imagePath)) {
                fs.unlinkSync(imagePath); // Xóa file ảnh
            }
        }

        // Bước 2: Xóa trong Database
        const sql = "DELETE FROM blogs WHERE id = ?";
        db.query(sql, [blogId], (err, result) => {
            if (err) return res.status(500).json(err);
            return res.json({ message: "Đã xóa bài viết và ảnh thành công!" });
        });
    });
});

// --- HÀM PHỤ TRỢ: XỬ LÝ TAGS (Dùng chung cho Create và Update) ---
function handleTags(blogId, tagsStr, db, res) {
    const tagList = tagsStr.split(',').map(tag => tag.trim()).filter(tag => tag);

    const process = async () => {
        try {
            for (const tagName of tagList) {
                const tagSlug = slugify(tagName, { lower: true });

                // Tìm hoặc Tạo Tag
                const tagId = await new Promise((resolve, reject) => {
                    db.query("SELECT id FROM tags WHERE slug = ?", [tagSlug], (err, rows) => {
                        if (err) reject(err);
                        if (rows.length > 0) resolve(rows[0].id);
                        else {
                            db.query("INSERT INTO tags (name, slug) VALUES (?, ?)", [tagName, tagSlug], (err, resTag) => {
                                if (err) reject(err);
                                else resolve(resTag.insertId);
                            });
                        }
                    });
                });

                // Link vào bảng trung gian
                await new Promise((resolve, reject) => {
                    db.query("INSERT IGNORE INTO blog_tags (blog_id, tag_id) VALUES (?, ?)", [blogId, tagId], (err) => {
                        if (err) reject(err); else resolve();
                    });
                });
            }

            db.commit((err) => {
                if (err) return db.rollback(() => res.status(500).json(err));
                res.json({ message: "Thao tác thành công!" });
            });
        } catch (error) {
            db.rollback(() => res.status(500).json({ error: "Lỗi xử lý Tags", details: error }));
        }
    };
    process();
}

// KHỞI ĐỘNG SERVER
app.listen(8000, () => {
    console.log('Server is running on port 8000');
});
