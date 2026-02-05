package com.example.client.view.screens

import android.net.Uri
import android.util.Log
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.client.model.data.Message
import com.example.client.model.data.User
import com.example.client.view.components.MessageBubble
import com.example.client.view.theme.*
import com.example.client.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

// Hằng số link ảnh mặc định để kiểm tra
const val DEFAULT_AVATAR_URL_CHAT = "https://i.imgur.com/6VBx3io.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenImprovedScreen(
    roomId: String,
    roomName: String, // Tên mặc định truyền từ Navigation
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val currentUserId by viewModel.currentUserIdState.collectAsState()

    // Tìm đối tượng phòng hiện tại
    val currentRoom = remember(rooms, roomId) {
        rooms.find { it.id == roomId }
    }

    // Tìm thông tin đối phương (Partner) để lấy avatarUrl
    val partner = remember(currentRoom, friends, currentUserId) {
        if (currentRoom?.isGroup == true) null
        else {
            currentRoom?.members?.find { it.id != currentUserId }
                ?: friends.find { it.id == currentRoom?.memberIds?.find { id -> id != currentUserId } }
        }
    }

    // Tên hiển thị trên thanh tiêu đề
    val displayTitle = remember(currentRoom, partner) {
        if (currentRoom?.isGroup == true && currentRoom.name.isNotBlank()) {
            currentRoom.name
        } else {
            partner?.let { it.fullName.ifBlank { it.username } } ?: roomName
        }
    }

    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Launcher để chọn ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendImage(context, it) }
    }

    // Thiết lập phòng chat hiện tại khi vào màn hình
    LaunchedEffect(roomId) {
        viewModel.setActiveRoom(roomId, displayTitle)
        viewModel.markRoomAsRead(roomId)
    }

    // Tự động cuộn xuống cuối khi có tin nhắn mới
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                roomName = displayTitle,
                isGroup = currentRoom?.isGroup == true,
                partner = partner,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Danh sách tin nhắn
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        currentUserId = currentUserId,
                        viewModel = viewModel,
                        onSeen = { viewModel.markAsSeen(message) }
                    )
                }
            }

            // Thanh nhập liệu
            ChatInputArea(
                textState = textState,
                onTextChange = {
                    textState = it
                    viewModel.onUserInputChanged(it)
                },
                onSendClick = {
                    if (textState.isNotBlank()) {
                        viewModel.sendMessage(textState)
                        textState = ""
                    }
                },
                onImageClick = {
                    imagePickerLauncher.launch("image/*")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    roomName: String,
    isGroup: Boolean,
    partner: User?,
    onBack: () -> Unit
) {
    // 🛠️ THÊM LOGIC GIẢI MÃ AVATAR GIỐNG PROFILE SCREEN
    val avatarUrl = partner?.avatarUrl
    val imageModel = remember(avatarUrl) {
        if (!avatarUrl.isNullOrBlank() && avatarUrl.startsWith("data:image")) {
            try {
                val base64String = avatarUrl.substringAfter(",")
                Base64.decode(base64String, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("CHAT_AVATAR_DEBUG", "Lỗi giải mã Base64: ${e.message}")
                avatarUrl
            }
        } else {
            avatarUrl
        }
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = if (isGroup) Color(0xFFE0F2F1) else TealLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!isGroup) {
                            if (!avatarUrl.isNullOrBlank() && avatarUrl != DEFAULT_AVATAR_URL_CHAT) {
                                AsyncImage(
                                    model = imageModel, // ✅ Sử dụng imageModel đã giải mã
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    onError = {
                                        Log.e("COIL_ERROR", "Không thể load avatar tại ChatTopBar")
                                    }
                                )
                            } else {
                                // Hiển thị chữ cái đầu tên (Ví dụ: H cho Hau)
                                val firstChar = roomName.trim().firstOrNull()?.uppercase() ?: "?"
                                Text(
                                    text = firstChar.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Icon nhóm mặc định
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = roomName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ChatInputArea(
    textState: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onImageClick) {
                Icon(Icons.Default.AddPhotoAlternate, "Gửi ảnh", tint = TealPrimary)
            }

            OutlinedTextField(
                value = textState,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập tin nhắn...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                maxLines = 4
            )

            Spacer(Modifier.width(4.dp))

            IconButton(onClick = onSendClick, enabled = textState.isNotBlank()) {
                Icon(
                    Icons.Default.Send,
                    "Gửi",
                    tint = if (textState.isNotBlank()) TealPrimary else Color.Gray
                )
            }
        }
    }
}