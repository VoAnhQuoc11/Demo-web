package com.example.client.view.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description // Icon file
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Cần cái này để mở link
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.client.model.data.Message
import com.example.client.utils.decodeBase64ToBitmap

@Composable
fun MessageBubble(message: Message, isMe: Boolean, onSeen: () -> Unit = {}) {
    val context = LocalContext.current // 1. Lấy context để dùng cho Intent mở file

    LaunchedEffect(Unit) {
        if (!isMe) onSeen()
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isMe) 18.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 18.dp
    )

    // 2. Xác định loại tin nhắn
    val isImage = message.type.equals("image", ignoreCase = true) || message.content.startsWith("data:image")
    val isFile = message.type.equals("file", ignoreCase = true)

    val imageBitmap = remember(message.content) {
        if (isImage) decodeBase64ToBitmap(message.content) else null
    }

    val myMessageColor = Color(0xFFFFC107)
    val otherMessageColor = Color(0xFFE4E6EB)
    val textColor = if (isMe) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isImage) Color.Transparent
                    else if (isMe) myMessageColor
                    else otherMessageColor,
                    shape = bubbleShape
                )
                // Chỉ bỏ padding nếu là ảnh, còn File vẫn cần padding cho đẹp
                .padding(if (isImage) 0.dp else 12.dp)
        ) {
            when {
                // TRƯỜNG HỢP 1: LÀ ẢNH
                isImage -> {
                    if (imageBitmap != null) {
                        AsyncImage(
                            model = imageBitmap,
                            contentDescription = "Gửi ảnh",
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 350.dp)
                                .clip(bubbleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (message.content.startsWith("http")) {
                        // Nếu ảnh là URL (do upload file)
                        AsyncImage(
                            model = "http://10.0.2.2:5000${message.content}", // Nhớ thay IP Server của bạn
                            contentDescription = "Ảnh từ server",
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 350.dp)
                                .clip(bubbleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Lỗi ảnh", color = Color.Red, modifier = Modifier.padding(8.dp))
                    }
                }

                // TRƯỜNG HỢP 2: LÀ FILE (Mới thêm)
                isFile -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            try {
                                // Ghép link server để tải file
                                // Lưu ý: Thay 10.0.2.2 bằng IP máy tính của bạn nếu chạy trên điện thoại thật
                                val fullUrl = "http://10.0.2.2:5000${message.content}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "File",
                            tint = if (isMe) Color.White else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = message.fileName ?: "Tài liệu", // Hiện tên file
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 200.dp)
                            )
                            Text(
                                text = "Nhấn để mở",
                                fontSize = 11.sp,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                    }
                }

                // TRƯỜNG HỢP 3: TEXT BÌNH THƯỜNG
                else -> {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Trạng thái đã xem
        if (isMe && message.status == "seen") {
            Text(
                text = "Đã xem",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 4.dp, top = 2.dp)
            )
        }
    }
}