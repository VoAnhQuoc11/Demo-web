// src/sockets/chatSocket.js
const Message = require('../models/Message');

module.exports = (io, socket) => {
    // --- 1. JOIN ROOM & LOAD HISTORY ---
    socket.on('join_room', async (roomId) => {
        socket.join(roomId);
        console.log(`[Socket] User ${socket.id} joined room: ${roomId}`);
        
        try {
            // Lấy tin nhắn cũ (Logic từ server test)
            const history = await Message.find({ roomId }).sort({ timestamp: 1 });
            
            // Format dữ liệu bằng hàm helper vừa tạo trong Model
            const formattedHistory = history.map(msg => msg.formatForClient());

            // Gửi sự kiện 'load_history' chuẩn format Android
            socket.emit('load_history', formattedHistory);
        } catch (error) {
            console.error("[Error] Load history failed:", error);
        }
    });

    // --- 2. GỬI TIN NHẮN TEXT ---
    socket.on('send_message', async (data) => {
        console.log('📩 Text received:', data.content);
        try {
            const newMessage = new Message({
                roomId: data.roomId,
                senderId: data.senderId, // Lấy từ client gửi lên (giống test server)
                content: data.content,
                type: 'TEXT',
                status: 'sent',
                timestamp: Date.now()
            });

            const savedMsg = await newMessage.save();
            
            // Emit lại cho cả phòng
            io.in(data.roomId).emit('receive_message', savedMsg.formatForClient());
        } catch (error) {
            console.error("[Error] Send text failed:", error);
        }
    });

    // --- 3. GỬI ẢNH (IMAGE) - TỪ CODE TEST ---
    socket.on('send_image', async (data) => {
        console.log('🖼️ Image received from:', data.senderId);
        try {
            const newMessage = new Message({
                roomId: data.roomId,
                senderId: data.senderId,
                content: data.imageBase64, // Client gửi key imageBase64 -> lưu vào content
                type: 'IMAGE',
                status: 'sent',
                timestamp: Date.now()
            });

            const savedMsg = await newMessage.save();

            io.in(data.roomId).emit('receive_message', savedMsg.formatForClient());
        } catch (error) {
            console.error("[Error] Send image failed:", error);
        }
    });

    // --- 4. TRẠNG THÁI GÕ (TYPING) ---
    socket.on('typing', (roomId) => {
        socket.to(roomId).emit('user_typing', socket.id);
    });

    socket.on('stop_typing', (roomId) => {
        socket.to(roomId).emit('user_stopped_typing');
    });

    // --- 5. ĐÃ XEM (SEEN) ---
    socket.on('mark_seen', async (data) => {
        const { roomId, messageId } = data;
        try {
            await Message.findByIdAndUpdate(messageId, { status: 'seen' });
            
            io.in(roomId).emit('message_seen_updated', { 
                messageId: messageId, 
                status: 'seen' 
            });
        } catch (error) {
            console.error("[Error] Mark seen failed:", error);
        }
    });
};