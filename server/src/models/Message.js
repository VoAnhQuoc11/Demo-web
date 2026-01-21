// src/models/Message.js
const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
    roomId: {
        type: String, // Đổi thành String để linh hoạt giống server test
        required: true
    },
    senderId: {
        type: String, // Đổi thành String để nhận ID từ Android gửi lên
        required: true
    },
    content: {
        type: String,
        required: true
    },
    type: {
        type: String,
        enum: ['TEXT', 'IMAGE', 'FILE', 'VOICE', 'CONTACT', 'SYSTEM'],
        default: 'TEXT'
    },
    status: {
        type: String,
        enum: ['sending', 'sent', 'delivered', 'seen'],
        default: 'sent'
    },
    // Giữ lại trường này để code test hoạt động (Test server dùng timestamp number)
    timestamp: { 
        type: Number, 
        default: () => Date.now() 
    }
}, { 
    timestamps: true // Vẫn giữ createdAt/updatedAt của Mongoose
});

// Helper format dữ liệu trả về cho Android (Lấy từ code test của bạn)
messageSchema.methods.formatForClient = function() {
    const date = new Date(this.timestamp || this.createdAt);
    const timeString = date.getHours().toString().padStart(2, '0') + ':' + 
                       date.getMinutes().toString().padStart(2, '0');

    return {
        id: this._id.toString(),
        roomId: this.roomId,
        senderId: this.senderId,
        content: this.content,
        type: this.type,
        createdAt: timeString,       // Android cần chuỗi "HH:mm"
        timestamp: this.timestamp,   // Android cần Long
        status: this.status
    };
};

const Message = mongoose.model('Message', messageSchema);
module.exports = Message;