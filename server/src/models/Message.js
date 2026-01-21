// server/src/models/Message.js
const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
    roomId: {
        type: String, 
        required: true
    },
    senderId: {
        type: String, 
        required: true
    },
    content: {
        type: String,
        required: true
    },
    type: {
        type: String,
        // Enum phải viết HOA để khớp với Client gửi lên (TEXT, IMAGE, FILE)
        enum: ['TEXT', 'IMAGE', 'FILE', 'VOICE', 'CONTACT', 'SYSTEM'], 
        default: 'TEXT'
    },
    // 👇 QUAN TRỌNG: Thêm trường này để lưu tên file (PDF/Word)
    fileName: { 
        type: String, 
        default: null 
    },
    status: {
        type: String,
        enum: ['sending', 'sent', 'delivered', 'seen'],
        default: 'sent'
    },
    timestamp: { 
        type: Number, 
        default: () => Date.now() 
    }
}, { 
    timestamps: true 
});

// Helper format dữ liệu trả về cho Android
messageSchema.methods.formatForClient = function() {
    const date = new Date(this.timestamp || this.createdAt);
    const timeString = date.getHours().toString().padStart(2, '0') + ':' + 
                       date.getMinutes().toString().padStart(2, '0');

    return {
        id: this._id ? this._id.toString() : Date.now().toString(),
        
        // 👇 SỬA LẠI CHO KHỚP SCHEMA (quan trọng)
        roomId: this.roomId,      // Schema là roomId -> dùng this.roomId
        senderId: this.senderId,  // Schema là senderId -> dùng this.senderId
        
        content: this.content || "",
        type: this.type || "TEXT",
        
        // Trả về tên file cho Client hiển thị
        fileName: this.fileName || null,
        
        createdAt: timeString,
        timestamp: this.timestamp,
        status: this.status
    };
};

const Message = mongoose.model('Message', messageSchema);
module.exports = Message;