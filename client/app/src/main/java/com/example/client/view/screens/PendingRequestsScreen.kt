package com.example.client.view.screens

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.client.model.data.User
import com.example.client.view.theme.TealLight
import com.example.client.view.theme.TealPrimary
import com.example.client.viewmodel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingRequestsScreen(
    contactViewModel: ContactViewModel,
    onFriendAccepted: () -> Unit,
    onBack: () -> Unit
) {
    val pendingRequests by contactViewModel.pendingRequests.collectAsState()

    LaunchedEffect(Unit) {
        contactViewModel.fetchPendingRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lời mời kết bạn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        if (pendingRequests.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Không có lời mời nào", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(pendingRequests) { user ->
                    PendingRequestItem(
                        user = user,
                        onAccept = {
                            contactViewModel.acceptFriendRequest(user.id) {
                                onFriendAccepted()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PendingRequestItem(user: User, onAccept: () -> Unit) {
    val avatarUrl = user.avatarUrl

    // 1. Kiểm tra URL avatar có hợp lệ để hiển thị ảnh không
    // Loại bỏ trường hợp rỗng hoặc link mặc định của Imgur
    val isAvatarValid = remember(avatarUrl) {
        !avatarUrl.isNullOrBlank() && avatarUrl != "https://i.imgur.com/6VBx3io.png"
    }

    // 2. Logic giải mã ảnh Base64 tương tự màn Profile
    val imageModel = remember(avatarUrl) {
        if (!avatarUrl.isNullOrBlank() && avatarUrl.startsWith("data:image")) {
            try {
                val base64String = avatarUrl.substringAfter(",")
                Base64.decode(base64String, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("PENDING_AVATAR", "Giải mã Base64 thất bại: ${e.message}")
                avatarUrl
            }
        } else {
            avatarUrl
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3. Phần hiển thị Avatar đã cập nhật logic
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                if (isAvatarValid) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(1.dp, TealPrimary, CircleShape), // Thêm viền nhẹ cho đẹp
                        contentScale = ContentScale.Crop,
                        onError = {
                            Log.e("COIL_ERROR", "Không thể load ảnh: $avatarUrl")
                        }
                    )
                } else {
                    // Hiển thị chữ cái đầu tiên của tên khi không có ảnh hoặc link mặc định
                    val displayName = if (user.fullName.isNotBlank()) user.fullName else user.username
                    val initial = if (displayName.isNotEmpty()) displayName.take(1).uppercase() else "?"

                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                val displayName = if (user.fullName.isNotBlank()) user.fullName else user.username
                Text(displayName, fontWeight = FontWeight.Bold)
                Text(user.phoneNumber, fontSize = 12.sp, color = Color.Gray)
            }

            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Đồng ý", fontSize = 12.sp)
            }
        }
    }
}