package com.example.client.view.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.model.data.User
import com.example.client.view.theme.TealLight
import com.example.client.view.theme.TealPrimary
import com.example.client.viewmodel.ContactViewModel // Import ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingRequestsScreen(
    contactViewModel: ContactViewModel, // Sử dụng ContactViewModel thay vì ChatViewModel
    onFriendAccepted: () -> Unit,       // Callback để báo cho AppNavigation biết cần refresh list chat
    onBack: () -> Unit
) {
    // Lấy dữ liệu từ ContactViewModel
    val pendingRequests by contactViewModel.pendingRequests.collectAsState()

    // Tự động load lại danh sách khi mở màn hình
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
                            // Gọi hàm accept từ ContactViewModel
                            contactViewModel.acceptFriendRequest(user.id) {
                                // Khi API thành công -> Gọi callback này để AppNavigation refresh ChatViewModel
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
    // 1. Thêm logic giải mã ảnh Base64 giống logic màn Profile
    val avatarUrl = user.avatarUrl
    val imageModel = remember(avatarUrl) {
        if (!avatarUrl.isNullOrBlank() && avatarUrl.startsWith("data:image")) {
            try {
                // Tách bỏ tiền tố "data:image/...;base64,"
                val base64String = avatarUrl.substringAfter(",")
                android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                android.util.Log.e("PENDING_AVATAR", "Lỗi giải mã: ${e.message}")
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
            // 2. Cập nhật phần hiển thị Avatar
            Surface(Modifier.size(48.dp), shape = CircleShape, color = TealLight) {
                Box(contentAlignment = Alignment.Center) {
                    // Kiểm tra nếu có avatarUrl thì dùng AsyncImage, ngược lại dùng chữ cái đầu
                    if (!avatarUrl.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = imageModel,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            onError = {
                                android.util.Log.e("COIL_ERROR", "Không thể load ảnh lời mời kết bạn")
                            }
                        )
                    } else {
                        val initial = if (user.username.isNotEmpty()) user.username.take(1).uppercase() else "?"
                        Text(initial, fontWeight = FontWeight.Bold, color = TealPrimary)
                    }
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
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Đồng ý", fontSize = 12.sp)
            }
        }
    }
}