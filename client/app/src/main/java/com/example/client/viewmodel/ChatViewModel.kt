package com.example.client.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.model.*
import com.example.client.model.data.ChatRoom
import com.example.client.model.data.Message
import com.example.client.model.data.User
import com.example.client.model.repository.SocketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject // Import quan trọng để sửa lỗi sendMessage
import java.util.UUID      // Import quan trọng để sửa lỗi UUID

class ChatViewModel(
    private val repository: SocketRepository = SocketRepository()
) : ViewModel() {

    private val apiService = RetrofitClient.instance

    val users: StateFlow<List<User>> = repository.users

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends

    private val _pendingRequests = MutableStateFlow<List<User>>(emptyList())
    val pendingRequests: StateFlow<List<User>> = _pendingRequests

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _rooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val rooms: StateFlow<List<ChatRoom>> = _rooms

    // Biến String thường, không phải StateFlow
    var currentUserId: String = ""
    private var authToken: String = ""

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var activeRoomId: String = ""

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.messagesByRoom.collectLatest { map ->
                if (activeRoomId.isNotBlank()) {
                    val list = map[activeRoomId] ?: emptyList()
                    _messages.value = list
                }
            }
        }

        viewModelScope.launch {
            repository.rooms.collect { socketRooms ->
                if (socketRooms.isNotEmpty()) {
                    _rooms.value = socketRooms
                }
            }
        }
    }

    // --- KẾT NỐI & TẢI DỮ LIỆU ---
    fun connect(token: String, userId: String) {
        if (token.isBlank()) return
        authToken = token
        currentUserId = userId
        Log.d("CHAT_VM", "Connect thành công. User: $userId")
        repository.connect(token)
        refreshData()
    }

    fun refreshData() {
        if (authToken.isBlank()) return
        fetchRooms()
        fetchFriends()
        fetchPendingRequests()
    }

    fun fetchRooms() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getRooms("Bearer $authToken")
                _rooms.value = response
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun fetchFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val friendsList = apiService.getFriends("Bearer $authToken")
                _friends.value = friendsList
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun fetchPendingRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val requests = apiService.getPendingRequests("Bearer $authToken")
                _pendingRequests.value = requests
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- TÌM KIẾM ---
    fun searchUsers(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank() || authToken.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isSearching.value = true
            try {
                var results = apiService.searchUsers("Bearer $authToken", trimmedQuery)
                if (results.isEmpty() && !trimmedQuery.startsWith("0") && !trimmedQuery.startsWith("+")) {
                    results = apiService.searchUsers("Bearer $authToken", "0$trimmedQuery")
                }
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    // --- PHÒNG CHAT & TIN NHẮN ---
    fun joinExistingRoom(room: ChatRoom) {
        // Sửa lỗi sai tên biến: dùng 'repository' thay vì 'socketRepository'
        repository.emit("join_room", room.id)

        // Gọi hàm tải tin nhắn cũ
        fetchMessages(room.id)

        setActiveRoom(room.id, room.name)
    }

    fun fetchMessages(roomId: String) {
        if (authToken.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Đảm bảo ApiService đã có hàm getMessages
                val historyMessages = apiService.getMessages("Bearer $authToken", roomId)
                _messages.value = historyMessages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setActiveRoom(roomId: String, roomName: String) {
        activeRoomId = roomId
        // Nếu SocketRepository chưa có syncMessages thì comment dòng dưới lại
        // repository.syncMessages(roomId)
    }

    fun sendMessage(roomId: String, content: String) {
        if (roomId.isBlank() || currentUserId.isBlank()) return

        // Sử dụng JSONObject (đã import ở trên)
        val messageData = JSONObject().apply {
            put("roomId", roomId)
            put("senderId", currentUserId) // currentUserId là String, không dùng .value
            put("content", content)
            put("type", "TEXT")
        }

        repository.emit("send_message", messageData)
    }

    fun sendImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
                    // Dùng repository.sendMessage (nếu repo có hàm này) hoặc dùng emit
                    // Ở đây giả định repo có hàm sendMessage hỗ trợ ảnh
                    repository.sendMessage(base64String, activeRoomId, currentUserId, "image")
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun markRoomAsRead(roomId: String) = repository.markRoomAsRead(roomId)

    // --- CÁC HÀM XỬ LÝ LOGIC NAVIGATION & GROUP (ĐÃ SỬA LỖI) ---

    // 1. Hàm startPrivateChat (Sửa lỗi AppNavigation)
    fun startPrivateChat(user: User): ChatRoom {
        val existingRoom = _rooms.value.find { room ->
            !room.isGroup && room.memberIds.contains(user.id)
        }
        if (existingRoom != null) return existingRoom

        return ChatRoom(
            id = "temp_private_${user.id}",
            name = user.fullName.ifBlank { user.username },
            isGroup = false,
            memberIds = listOf(currentUserId, user.id),
            lastMessage = "",
            lastUpdated = System.currentTimeMillis()
        )
    }

    // 2. Hàm createGroup (Sửa lỗi AppNavigation)
    fun createGroup(name: String, memberIds: List<String>): ChatRoom {
        val tempId = "temp_group_${UUID.randomUUID()}"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                apiService.createGroup("Bearer $authToken", CreateGroupRequest(name, memberIds))
                fetchRooms()
            } catch (e: Exception) { e.printStackTrace() }
        }

        return ChatRoom(
            id = tempId,
            name = name,
            isGroup = true,
            memberIds = memberIds + currentUserId,
            lastMessage = "Nhóm mới",
            lastUpdated = System.currentTimeMillis()
        )
    }

    // 3. Hàm disconnect (Sửa lỗi AppNavigation)
    fun disconnect() {
        try {
            repository.disconnect()
        } catch (e: Exception) { }

        currentUserId = ""
        authToken = ""
        _rooms.value = emptyList()
        _messages.value = emptyList()
    }

    // --- CÁC HÀM QUẢN LÝ BẠN BÈ & MEMBER ---

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Đảm bảo class FriendRequest đã có trong ApiService.kt
                apiService.sendFriendRequest("Bearer $authToken", FriendRequest(userId))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun acceptFriendRequest(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                apiService.acceptFriendRequest("Bearer $authToken", FriendRequest(userId))
                refreshData()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addMember(roomId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                apiService.addMember("Bearer $authToken", roomId, MemberRequest(userId))
                // Nếu repo có hàm addMember local thì gọi, không thì fetchRooms
                // repository.addMember(roomId, userId)
                fetchRooms()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    fun markAsSeen(message: Message) {
        // Hiện tại logic này có thể để trống hoặc chờ cập nhật Server
        // repository.markAsSeen(message.id)
    }

    // 2. Hàm onUserInputChanged (Xử lý lỗi Unresolved reference 'onUserInputChanged')
    fun onUserInputChanged(text: String) {
        // Logic gửi trạng thái "Đang soạn tin..."
        // if (activeRoomId.isNotBlank()) repository.sendTyping(activeRoomId)
    }

    // 3. Hàm sendMessage quá tải (Xử lý lỗi No value passed for parameter 'content')
    // Hàm này cho phép gọi sendMessage chỉ với 1 tham số (content), tự động lấy roomId đang active
    fun sendMessage(content: String) {
        if (activeRoomId.isNotBlank()) {
            sendMessage(activeRoomId, content)
        } else {
            Log.e("ChatViewModel", "Không thể gửi tin nhắn: Chưa có phòng nào đang mở (activeRoomId is empty)")
        }
    }
    // Thêm vào ChatViewModel
    fun joinRoomById(roomId: String) {
        repository.emit("join_room", roomId)
        fetchMessages(roomId)
        setActiveRoom(roomId, "") // Name có thể cập nhật sau
    }

    // Các hàm wrapper gọi xuống repjoinRoomByIdository
    fun leaveRoom(roomId: String) = repository.leaveRoom(roomId)
    fun pinRoom(roomId: String) = repository.pinRoom(roomId)
    fun unpinRoom(roomId: String) = repository.unpinRoom(roomId)
    fun archiveRoom(roomId: String) = repository.archiveRoom(roomId)
    fun muteRoom(roomId: String) = repository.muteRoom(roomId)
    fun unmuteRoom(roomId: String) = repository.unmuteRoom(roomId)
    fun renameGroup(roomId: String, newName: String) = repository.renameGroup(roomId, newName)
    fun kickMember(roomId: String, userId: String) = repository.kickMember(roomId, userId)
    fun transferAdmin(roomId: String, newAdminId: String) = repository.transferAdmin(roomId, newAdminId)
}