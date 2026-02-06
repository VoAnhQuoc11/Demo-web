package com.example.client.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.client.model.data.Message
import com.example.client.utils.decodeBase64ToBitmap
import com.example.client.view.theme.*
import com.example.client.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

// Link ảnh mặc định đồng bộ với hệ thống
const val DEFAULT_AVATAR_URL_BUBBLE = "https://i.imgur.com/6VBx3io.png"

@Composable
fun MessageBubble(
    message: Message,
    currentUserId: String,
    viewModel: ChatViewModel,
    onSeen: () -> Unit = {}
) {
    // 1. Bạn có thể giữ dòng này để dùng khi tin nhắn không kèm thông tin (dự phòng)
    val friends by viewModel.friends.collectAsState()

    val isMe = remember(message.senderId, currentUserId) {
        message.senderId == currentUserId
    }

    // 2. LOGIC HIỂN THỊ TÊN: Ưu tiên dữ liệu đính kèm tin nhắn từ bất kỳ ai
    val finalSenderName = remember(message.senderName, message.senderId, friends) {
        when {
            // Nếu tin nhắn có kèm tên (do server gửi), dùng luôn tên đó
            message.senderName.isNotBlank() -> message.senderName

            // Nếu không có, mới tìm trong danh sách bạn bè (dự phòng cho tin nhắn cũ)
            else -> {
                val friend = friends.find { it.id == message.senderId }
                friend?.let { it.fullName.ifBlank { it.username } } ?: "Người dùng"
            }
        }
    }

    // 3. LOGIC HIỂN THỊ AVATAR
    val finalAvatarUrl = remember(message.senderAvatar, message.senderId, friends) {
        if (message.senderAvatar.isNotBlank()) {
            message.senderAvatar
        } else {
            // Dự phòng tìm avatar trong danh sách bạn bè
            friends.find { it.id == message.senderId }?.avatarUrl ?: ""
        }
    }
    // 🛠️ 2. THÊM LOGIC GIẢI MÃ BASE64 CHO AVATAR NGƯỜI GỬI
    val avatarModel = remember(finalAvatarUrl) {
        if (finalAvatarUrl.startsWith("data:image")) {
            try {
                // Tách bỏ tiền tố metadata và giải mã sang ByteArray
                val base64String = finalAvatarUrl.substringAfter(",")
                android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                finalAvatarUrl
            }
        } else {
            finalAvatarUrl
        }
    }

    LaunchedEffect(isMe) {
        if (!isMe) onSeen()
    }

    val isImage = remember(message.content) {
        message.type.equals("image", ignoreCase = true) ||
                message.content.startsWith("data:image")
    }

    val timeText = remember(message.timestamp) {
        if (message.timestamp > 0) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        } else ""
    }

    val imageBitmap = remember(message.content, isImage) {
        if (isImage) decodeBase64ToBitmap(message.content) else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.widthIn(max = 340.dp),
            verticalAlignment = Alignment.Top, // Căn Top để Tên ngang hàng với Avatar
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            // Hiển thị Avatar (Chỉ khi không phải là mình)
            if (!isMe) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = TealLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // ✅ SỬ DỤNG avatarModel ĐÃ GIẢI MÃ
                        if (finalAvatarUrl.isNotBlank() && finalAvatarUrl != DEFAULT_AVATAR_URL_BUBBLE) {
                            AsyncImage(
                                model = avatarModel, // <--- Cập nhật ở đây
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                onError = {
                                    android.util.Log.e("COIL_ERROR", "Lỗi load avatar tin nhắn của: $finalSenderName")
                                }
                            )
                        } else {
                            val firstChar = finalSenderName.trim().firstOrNull()?.uppercase() ?: "?"
                            Text(
                                text = firstChar.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
            }

            // Column bao gồm Tên và Nội dung tin nhắn
            Column(
                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
            ) {
                // Hiển thị TÊN (Nằm ngang Avatar)
                if (!isMe) {
                    Text(
                        text = finalSenderName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Bong bóng tin nhắn (Nằm dưới tên)
                Surface(
                    shape = RoundedCornerShape(
                        topStart = if (isMe) 16.dp else 2.dp, // Bo góc ít hơn ở phía tên người gửi
                        topEnd = if (isMe) 2.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    color = when {
                        isImage -> Color.Transparent
                        isMe -> TealPrimary
                        else -> Color(0xFFF0F0F0) // Màu xám nhẹ cho tin nhắn người khác
                    },
                    shadowElevation = if (isImage) 0.dp else 1.dp
                ) {
                    if (isImage && imageBitmap != null) {
                        AsyncImage(
                            model = imageBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .widthIn(max = 240.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = message.content,
                            color = if (isMe) Color.White else Color.Black,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                // Giờ gửi tin nhắn (Nằm dưới bong bóng)
                if (timeText.isNotEmpty()) {
                    Text(
                        text = timeText,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }
        }
    }
}