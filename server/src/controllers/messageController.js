// src/controllers/messageController.js
const Message = require('../models/Message');

exports.getMessages = async (req, res) => {
    try {
        const { roomId } = req.params; // Lấy roomId từ URL

        // Tìm tin nhắn theo roomId
        const messages = await Message.find({ roomId })
            .sort({ createdAt: 1 }) // Sắp xếp: 1 là Tăng dần (Cũ trước, Mới sau)
            .populate('senderId', 'username avatarUrl fullName'); // "Nối bảng": Lấy thêm thông tin người gửi

        res.json(messages);

    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Lỗi server: " + error.message });
    }
};
exports.searchMessages = async (req, res) => {
    try {
        const { roomId, keyword } = req.query;

        console.log(`🔍 Đang tìm: "${keyword}" trong phòng: ${roomId}`); // Log để debug

        if (!roomId || !keyword) {
            return res.status(400).json({ message: 'Thiếu info' });
        }

        const messages = await Message.find({
            roomId: roomId,
            content: { $regex: keyword, $options: 'i' }, // 'i' = không phân biệt hoa thường
            
            // 👇 SỬA DÒNG NÀY:
            // Cách 1: Chấp nhận cả chữ hoa và thường
            type: { $in: ['TEXT', 'text'] } 
            
            // Cách 2 (Nếu muốn tìm cả trong file tên file, caption...):
            // Bỏ luôn dòng 'type' đi cũng được
        }).sort({ timestamp: -1 });

        console.log(`✅ Tìm thấy: ${messages.length} kết quả`); // Log kết quả

        const formattedMessages = messages.map(msg => 
            msg.formatForClient ? msg.formatForClient() : msg
        );

        res.json(formattedMessages);
    } catch (err) {
        console.error("Lỗi tìm kiếm:", err);
        res.status(500).json({ message: 'Lỗi server' });
    }
};