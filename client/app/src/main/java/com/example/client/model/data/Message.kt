// Message.kt
package com.example.client.model.data
import org.json.JSONObject

data class Message(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val content: String = "",
    val type: String = "TEXT",     // "TEXT", "IMAGE", "FILE"
    val fileName: String? = null,  // <--- 1. THÊM DÒNG NÀY (để lưu tên file PDF/Word)
    val createdAt: String = "",
    val timestamp: Long = 0L,
    val status: String = "sent"
) {
    companion object {
        fun fromJson(json: JSONObject): Message {
            return Message(
                // Lưu ý: MongoDB thường trả về "_id", kiểm tra lại server gửi "id" hay "_id"
                id = if (json.has("id")) json.optString("id") else json.optString("_id"),

                roomId = json.optString("roomId"),
                senderId = if (json.has("senderId")) json.optString("senderId") else json.optString("sender"),
                content = json.optString("content"),
                type = json.optString("type", "TEXT"),

                // <--- 2. THÊM DÒNG NÀY ĐỂ LẤY TÊN FILE TỪ JSON
                fileName = json.optString("fileName", null),

                createdAt = json.optString("createdAt", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                status = json.optString("status", "sent")
            )
        }
    }
}