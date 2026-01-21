package com.example.client.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.api.AuthService
import com.example.client.api.RetrofitClient // [Mới] Import RetrofitClient
import com.example.client.model.data.Message
import com.example.client.model.repository.SocketRepository
import com.example.client.utils.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull // [Mới] Import extension này
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.jvm.java
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SocketRepository()

    // [Sửa] Khởi tạo authService để dùng API upload
    // Hãy đảm bảo RetrofitClient của bạn có biến 'instance' hoặc hàm 'create()' trả về AuthService
    private val authService = RetrofitClient.instance.create(AuthService::class.java)

    val messages: StateFlow<List<Message>> = repository.messages

    val currentUserId: String
    val currentRoomId = "room_abc" // Phòng chat cố định (để test)

    private val _typingUser = MutableStateFlow<String?>(null)
    val typingUser: StateFlow<String?> = _typingUser

    private var typingHandler: Handler = Handler(Looper.getMainLooper())
    private val stopTypingRunnable = Runnable {
        repository.sendStopTyping(currentRoomId)
        _typingUser.value = null
    }

    init {
        val prefs = application.getSharedPreferences("chat_app_prefs", Context.MODE_PRIVATE)
        var savedId = prefs.getString("user_id", null)

        if (savedId == null) {
            savedId = "user_${UUID.randomUUID().toString().substring(0, 5)}"
            prefs.edit().putString("user_id", savedId).apply()
        }
        currentUserId = savedId!! // !! vì đã check null ở trên

        // Kết nối Socket
        repository.connect()

        // Lắng nghe lịch sử
        repository.socket.on("load_history") { args ->
            if (args.isNotEmpty()) {
                val jsonArray = args[0] as JSONArray
                val historyList = ArrayList<Message>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val message = Message.fromJson(jsonObject)
                    historyList.add(message)
                }

                viewModelScope.launch {
                    repository.updateMessageList(historyList)
                }
            }
        }

        joinRoom(currentRoomId)

        repository.socket.on("user_typing") { args ->
            val userId = args[0] as String
            if (userId != currentUserId) {
                _typingUser.value = userId
            }
        }

        repository.socket.on("user_stopped_typing") {
            _typingUser.value = null
        }

        repository.socket.on("message_seen_updated") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val messageId = data.optString("messageId")
                repository.updateMessageStatus(messageId, "seen")
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isNotBlank()) {
            repository.sendMessage(content, currentRoomId, currentUserId, "TEXT")
        }
    }

    fun joinRoom(roomId: String) {
        repository.joinRoom(roomId)
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }

    fun onUserInputChanged(text: String) {
        if (text.isNotBlank()) {
            // [Sửa] Truy cập socket qua repository
            repository.socket.emit("typing", currentRoomId)

            typingHandler.removeCallbacks(stopTypingRunnable)
            typingHandler.postDelayed(stopTypingRunnable, 2000)
        }
    }

    fun markAsSeen(message: Message) {
        if (message.senderId != currentUserId && message.status != "seen") {
            val data = JSONObject()
            data.put("roomId", currentRoomId)
            data.put("messageId", message.id)
            // [Sửa] Truy cập socket qua repository
            repository.socket.emit("mark_seen", data)
        }
    }

    fun sendImage(context: Context, uri: Uri) {
        val base64Image = uriToBase64(context, uri)
        if (base64Image != null) {
            val content = "data:image/jpeg;base64,$base64Image"
            repository.sendMessage(content, currentRoomId, currentUserId, "IMAGE")
        }
    }

    private fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- CHỨC NĂNG GỬI FILE MỚI ---
    fun uploadAndSendFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // 1. Chuyển Uri thành File
                val file = FileUtils.getFileFromUri(context, uri)

                if (file != null) {
                    // 2. Tạo Request Body
                    val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    // 3. Upload lên Server (Sử dụng biến authService đã khai báo ở trên)
                    val response = authService.uploadFile(body)

                    // 4. Gửi Socket
                    val messageData = JSONObject().apply {
                        put("room", currentRoomId)
                        put("sender", currentUserId)
                        put("content", response.url) // Link file từ server trả về
                        put("type", "file")          // Đánh dấu là file
                        put("fileName", file.name)   // Tên file gốc
                    }

                    // [Sửa] Truy cập socket qua repository
                    repository.socket.emit("send_message", messageData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Bạn có thể thêm StateFlow để hiển thị lỗi lên UI nếu muốn
            }
        }
    }
}