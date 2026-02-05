package com.example.client.view.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.client.model.data.User
import com.example.client.view.theme.*
import com.example.client.viewmodel.ContactViewModel

// Hằng số link ảnh mặc định để kiểm tra đồng bộ hệ thống
const val DEFAULT_AVATAR_ADD_CONTACT = "https://i.imgur.com/6VBx3io.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewContactScreen(
    viewModel: ContactViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Quan sát dữ liệu từ ViewModel
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // Biến để kiểm soát việc đã bấm nút tìm kiếm hay chưa
    var searchPerformed by remember { mutableStateOf(false) }

    // Xóa kết quả cũ khi thoát/vào màn hình
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSearchResults()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm bạn bè", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            Text(
                "Tìm kiếm người dùng",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isBlank()) {
                        viewModel.clearSearchResults()
                        searchPerformed = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập số điện thoại...") },
                leadingIcon = {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = TealPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.clearSearchResults()
                            searchPerformed = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isSearching,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    cursorColor = TealPrimary
                )
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val query = searchQuery.trim()
                    if (query.isNotEmpty()) {
                        searchPerformed = true
                        viewModel.searchUsers(query)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSearching && searchQuery.trim().isNotEmpty()
            ) {
                if (isSearching) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("TÌM KIẾM", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            // HIỂN THỊ KẾT QUẢ
            Box(modifier = Modifier.fillMaxSize()) {
                if (searchResults.isNotEmpty()) {
                    Column {
                        Text(
                            "Kết quả tìm thấy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Spacer(Modifier.height(16.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { user ->
                                SearchResultItem(
                                    user = user,
                                    onAddFriend = {
                                        viewModel.sendFriendRequest(user.id) {
                                            // Sau khi gửi lời mời có thể quay lại hoặc hiện thông báo
                                            onBack()
                                        }
                                    }
                                )
                            }
                        }
                    }
                } else if (searchPerformed && !isSearching) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SentimentDissatisfied, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("Không tìm thấy người dùng này", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(user: User, onAddFriend: () -> Unit) {
    // 1. Tối ưu hóa Model cho AsyncImage giống logic màn Profile
    // Giải mã Base64 thành ByteArray để Coil hiển thị ổn định hơn
    val imageModel = remember(user.avatarUrl) {
        val avatarUrl = user.avatarUrl
        if (!avatarUrl.isNullOrBlank() && avatarUrl.startsWith("data:image")) {
            try {
                // Tách bỏ phần tiền tố "data:image/...;base64,"
                val base64String = avatarUrl.substringAfter(",")
                android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("SEARCH_AVATAR_DEBUG", "Giải mã Base64 thất bại: ${e.message}")
                avatarUrl
            }
        } else {
            avatarUrl // Nếu là URL bình thường (https://...)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 2. PHẦN HIỂN THỊ AVATAR
            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = TealLight) {
                Box(contentAlignment = Alignment.Center) {
                    val avatarUrl = user.avatarUrl

                    // Kiểm tra điều kiện hiển thị ảnh (không trống và không phải link mặc định)
                    if (!avatarUrl.isNullOrBlank() && avatarUrl != DEFAULT_AVATAR_ADD_CONTACT) {
                        AsyncImage(
                            model = imageModel, // Sử dụng imageModel đã giải mã ở trên
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            onError = {
                                Log.e("COIL_ERROR", "Không thể load ảnh search cho: ${user.username}")
                            }
                        )
                    } else {
                        // Hiển thị chữ cái đầu nếu không có ảnh
                        val displayName = user.fullName.ifBlank { user.username }
                        Text(
                            text = displayName.trim().take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName.ifBlank { user.username },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (user.phoneNumber.isNotBlank()) user.phoneNumber else "@${user.username}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // LOGIC KIỂM TRA TRẠNG THÁI BẠN BÈ (Giữ nguyên)
            if (user.isFriend) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Đã kết bạn", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                Button(
                    onClick = onAddFriend,
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Kết bạn", fontSize = 12.sp)
                }
            }
        }
    }
}