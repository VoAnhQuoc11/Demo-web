package com.example.client.model.repository

import android.util.Log
import com.example.client.model.data.ChatRoom
import com.example.client.model.data.Message
import com.example.client.model.data.User
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.concurrent.ConcurrentHashMap

class SocketRepository(
    // Lưu ý: Dùng 10.0.2.2 cho Emulator, hoặc IP LAN (ví dụ 192.168.1.x) cho máy thật
    private val socketUrl: String = "http://192.168.1.13:3000/"
) {

    private val TAG = "SocketRepo"

    // StateFlows
    private val _users = MutableStateFlow<List<User>>(emptyList())
    private val _rooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    private val _messagesByRoom = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

    val users: StateFlow<List<User>> = _users
    val rooms: StateFlow<List<ChatRoom>> = _rooms
    val messagesByRoom: StateFlow<Map<String, List<Message>>> = _messagesByRoom

    private val socketScope = CoroutineScope(Dispatchers.IO)
    private var socket: Socket? = null

    // Cache tin nhắn
    private val messagesCache = ConcurrentHashMap<String, MutableList<Message>>()

    fun connect(token: String) {
        if (socket?.connected() == true) return


        // Cấu hình Socket
        val opts = IO.Options().apply {
            transports = arrayOf("websocket")
            // Gửi token cả ở query và auth để chắc chắn server nhận được
            query = "token=$token"
            auth = mapOf("token" to token)
            reconnection = true
            reconnectionAttempts = 10
            reconnectionDelay = 1000
        }

        try {
            socket = IO.socket(socketUrl, opts)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }

        socket?.apply {
            // 1. KẾT NỐI THÀNH CÔNG
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, " Socket Connected Successfully!")
                socketScope.launch {
                    socket?.emit("join", "")
                    socket?.emit("list_users")
                    socket?.emit("list_rooms")
                }
            }

            // 2. LỖI KẾT NỐI (Để debug)
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                if (args.isNotEmpty()) {
                    Log.e(TAG, " Socket Connect Error: ${args[0]}")
                }
            }

            // 3. Nhận danh sách User Online
            on("online_users") { args ->
                if (args.isNotEmpty() && args[0] is JSONArray) {
                    val arr = args[0] as JSONArray
                    _users.value = parseUsersArray(arr)
                }
            }

            // 4. Nhận danh sách Room
            on("room_list") { args ->
                if (args.isNotEmpty() && args[0] is JSONArray) {
                    val arr = args[0] as JSONArray
                    _rooms.value = parseRoomsArray(arr)
                }
            }

            // 5. Nhận tin nhắn mới
            on("receive_message") { args ->
                Log.d(TAG, "📩 New Message Received")
                if (args.isNotEmpty()) {
                    val obj = args[0]
                    if (obj is JSONObject) {
                        try {
                            val message = Message.fromJson(obj)
                            appendMessage(message)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing message: ${e.message}")
                        }
                    }
                }
            }

            // 6. Nhận lịch sử chat
            on("load_history") { args ->
                Log.d(TAG, " History Loaded")
                if (args.isNotEmpty()) {
                    val data = args[0] // Server trả về mảng trực tiếp
                    val list = mutableListOf<Message>()

                    if (data is JSONArray) {
                        for (i in 0 until data.length()) {
                            val m = data.optJSONObject(i) ?: continue
                            list.add(Message.fromJson(m))
                        }

                        if (list.isNotEmpty()) {
                            val roomId = list[0].roomId
                            // Cập nhật Cache và StateFlow
                            messagesCache[roomId] = list
                            _messagesByRoom.value = HashMap(messagesCache)
                        }
                    }
                }
            }

            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "⚠️ Socket Disconnected")
            }
            on("room_updated") { args ->
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    val obj = args[0] as JSONObject
                    try {
                        // 1. Parse JSON nhận được thành đối tượng ChatRoom
                        val newRoom = parseSingleRoom(obj)

                        // 2. Cập nhật danh sách phòng hiện tại trong StateFlow
                        val currentRooms = _rooms.value.toMutableList()

                        // Nếu phòng đã tồn tại thì cập nhật, nếu chưa thì thêm vào đầu
                        val index = currentRooms.indexOfFirst { it.id == newRoom.id }
                        if (index != -1) {
                            currentRooms[index] = newRoom
                        } else {
                            currentRooms.add(0, newRoom)
                        }

                        // 3. Đẩy list mới vào StateFlow để UI (Compose) tự động vẽ lại
                        _rooms.value = currentRooms
                        Log.d(TAG, "Cập nhật danh sách phòng từ room_updated: ${newRoom.name}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Lỗi parse room_updated: ${e.message}")
                    }
                }
            }

            // Bắt đầu kết nối
            connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    // --- CÁC HÀM EMIT ---

    fun joinRoom(roomId: String) {
        Log.d(TAG, "➡️ Joining room: $roomId")
        socket?.emit("join_room", roomId)
    }

    fun syncMessages(roomId: String) {
        Log.d(TAG, "🔄 Syncing messages for: $roomId")
        socket?.emit("sync_messages", roomId)
    }
    fun requestRooms(userId: String) {
        // Lưu ý: Server của bạn dùng sự kiện "list_rooms"
        socket?.emit("list_rooms", userId)
        Log.d("SocketRepo", "Đã emit list_rooms cho userId: $userId")
    }

    fun sendMessage(content: String, roomId: String, userId: String, type: String) {
        val payload = JSONObject()
        payload.put("roomId", roomId)
        payload.put("senderId", userId) // Đảm bảo dùng senderId cho đồng bộ với server
        payload.put("type", type.uppercase()) // "TEXT" hoặc "IMAGE"

        if (type.uppercase() == "IMAGE") {
            // Nếu là ảnh, gửi vào trường imageBase64 như server yêu cầu
            payload.put("imageBase64", content)
            payload.put("content", "📷 Hình ảnh") // Gửi kèm một nội dung text để tránh server báo lỗi empty
        } else {
            // Nếu là tin nhắn thường
            payload.put("content", content)
        }

        Log.d("SocketRepo", "Sending $type message to room $roomId")
        socket?.emit("send_message", payload)
    }
    // Hàm lấy danh sách người dùng online
    fun requestOnlineUsers() {
        socket?.emit("join", null) // Server lắng nghe sự kiện 'join' để trả về online_users
        Log.d(TAG, "Đã gửi yêu cầu lấy danh sách online users")
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---
    fun createGroup(name: String, memberIds: List<String>): ChatRoom {
        val payload = JSONObject()
        payload.put("name", name)
        payload.put("members", JSONArray(memberIds))
        socket?.emit("create_group", payload)
        return ChatRoom(id = "temp", name = name, isGroup = true)
    }
    // Thêm vào SocketRepository.kt
    fun createChatGroup(name: String, memberIds: List<String>) {
        val payload = JSONObject().apply {
            put("name", name)
            put("members", JSONArray(memberIds))
            put("isGroup", true)
        }
        // Gửi sự kiện tạo nhóm tới Server
        socket?.emit("create_group", payload)
        Log.d("SocketRepo", "Đã gửi yêu cầu tạo nhóm mới: $name")
    }
    fun leaveRoom(roomId: String) = socket?.emit("leave_room", JSONObject().put("roomId", roomId))
    fun pinRoom(roomId: String) = socket?.emit("pin_room", JSONObject().put("roomId", roomId))
    fun muteRoom(roomId: String) = socket?.emit("mute_room", JSONObject().put("roomId", roomId))
    fun archiveRoom(roomId: String) = socket?.emit("archive_room", JSONObject().put("roomId", roomId))
    fun markRoomAsRead(roomId: String) { }

    fun addMember(roomId: String, userId: String) {
        socket?.emit("add_member", JSONObject().put("roomId", roomId).put("userId", userId))
    }
    fun kickMember(roomId: String, userId: String) {
        socket?.emit("kick_member", JSONObject().put("roomId", roomId).put("userId", userId))
    }
    fun renameGroup(roomId: String, name: String) { /* Implement logic */ }
    fun transferAdmin(roomId: String, userId: String) { /* Implement logic */ }
    fun unpinRoom(roomId: String) { /* Implement logic */ }
    fun unmuteRoom(roomId: String) { /* Implement logic */ }

    fun ensurePrivateRoom(currentUserId: String, user: User): ChatRoom {
        return ChatRoom(id = "temp_private", name = user.fullName)
    }

    // --- PARSERS ---
    // Trong SocketRepository.kt - Tìm đến hàm parseUsersArray và sửa như sau:

    private fun parseUsersArray(arr: JSONArray): List<User> {
        val out = mutableListOf<User>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("_id").ifBlank { obj.optString("id") }

            // --- ĐOẠN SỬA QUAN TRỌNG: Đọc mảng friends ---
            val friendsJson = obj.optJSONArray("friends")
            val friendList = mutableListOf<String>()
            if (friendsJson != null) {
                for (j in 0 until friendsJson.length()) {
                    friendList.add(friendsJson.optString(j))
                }
            }
            // --------------------------------------------

            out.add(User(
                id = id,
                username = obj.optString("username"),
                fullName = obj.optString("fullName"),
                avatarUrl = obj.optString("avatarUrl", ""),
                phoneNumber = obj.optString("phoneNumber", ""),
                isOnline = obj.optBoolean("isOnline", false),
                friends = friendList // Gán mảng friendList vào User object
            ))
        }
        return out
    }
    // ... các StateFlow có sẵn

    // THÊM DÒNG NÀY:
    private val _dataRefreshEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0)
    val dataRefreshEvent = _dataRefreshEvent.asSharedFlow()
    // THÊM HÀM NÀY:
    fun triggerDataRefresh() {
        socketScope.launch {
            _dataRefreshEvent.emit(Unit)
            Log.d(TAG, "Đã phát tín hiệu làm mới dữ liệu hệ thống")
        }
    }


    private fun parseRoomsArray(arr: JSONArray): List<ChatRoom> {
        val out = mutableListOf<ChatRoom>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("_id").ifBlank { obj.optString("id") }
            val memberIds = mutableListOf<String>()
            val membersJson = obj.optJSONArray("members")
            if (membersJson != null) {
                for(j in 0 until membersJson.length()) {
                    val m = membersJson.opt(j)
                    if(m is String) memberIds.add(m)
                    else if(m is JSONObject) memberIds.add(m.optString("_id"))
                }
            }

            out.add(ChatRoom(
                id = id,
                name = obj.optString("name"),
                isGroup = obj.optBoolean("isGroup", false),
                memberIds = memberIds,
                lastMessage = "",
                isPinned = obj.optBoolean("isPinned", false),
                isMuted = obj.optBoolean("isMuted", false),
                isArchived = obj.optBoolean("isArchived", false)
            ))
        }
        return out
    }
    private fun parseSingleRoom(obj: JSONObject): ChatRoom {
        val id = obj.optString("_id").ifBlank { obj.optString("id") }
        val memberIds = mutableListOf<String>()
        val membersJson = obj.optJSONArray("members")
        if (membersJson != null) {
            for(j in 0 until membersJson.length()) {
                val m = membersJson.opt(j)
                if(m is String) memberIds.add(m)
                else if(m is JSONObject) memberIds.add(m.optString("_id"))
            }
        }
        return ChatRoom(
            id = id,
            name = obj.optString("name"),
            isGroup = obj.optBoolean("isGroup", false),
            memberIds = memberIds,
            lastMessage = obj.optString("lastMessage", "Nhóm mới tạo"),
            isPinned = obj.optBoolean("isPinned", false)
        )
    }


    private fun appendMessage(message: Message) {
        // 1. Lấy danh sách hiện tại hoặc tạo mới
        val currentList = messagesCache[message.roomId] ?: mutableListOf()

        // 2. Kiểm tra trùng lặp (Log id để debug nếu vẫn lỗi)
        if (currentList.none { it.id == message.id }) {
            currentList.add(message)
            messagesCache[message.roomId] = currentList

            // 3. Quan trọng: Tạo một bản sao mới hoàn toàn của Map
            // và bản sao mới của List bên trong để StateFlow nhận diện thay đổi
            val updatedMap = HashMap<String, List<Message>>()
            messagesCache.forEach { (key, value) ->
                updatedMap[key] = value.toList() // .toList() tạo bản sao mới của danh sách
            }
            _messagesByRoom.value = updatedMap
            Log.d(TAG, "Đã cập nhật tin nhắn mới vào phòng ${message.roomId}. Tổng: ${currentList.size}")
        } else {
            Log.d(TAG, "Tin nhắn bị trùng ID: ${message.id}, bỏ qua.")
        }
    }
}