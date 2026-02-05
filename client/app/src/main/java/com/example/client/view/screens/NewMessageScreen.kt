@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.client.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.client.model.data.User
import com.example.client.view.theme.*
import com.example.client.viewmodel.ChatViewModel
import com.example.client.viewmodel.ContactViewModel

// Link ảnh mặc định để kiểm tra đồng bộ hệ thống
const val DEFAULT_AVATAR_NEW_MESSAGE = "https://i.imgur.com/6VBx3io.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(
    chatViewModel: ChatViewModel,
    contactViewModel: ContactViewModel, // Vẫn giữ tham số để tránh lỗi điều hướng, nhưng không dùng remote search
    onBack: () -> Unit,
    onUserSelected: (User) -> Unit,
    onAddContact: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Lấy danh sách bạn bè từ ChatViewModel (Local Data)
    val friends by chatViewModel.friends.collectAsState()

    // State quản lý tạo nhóm
    var selectedUserIds by remember { mutableStateOf(setOf<String>()) }
    var isGroupMode by remember { mutableStateOf(false) }
    var showGroupNameDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        chatViewModel.refreshData()
    }

    // Logic lọc danh sách: CHỈ tìm kiếm trong danh sách friends hiện có
    val displayedUsers = remember(searchQuery, friends) {
        val allFriendsExceptMe = friends.filter { it.id != chatViewModel.currentUserId }
        if (searchQuery.isBlank()) {
            allFriendsExceptMe
        } else {
            allFriendsExceptMe.filter { user ->
                val fullName = user.fullName.ifBlank { user.username }
                fullName.contains(searchQuery, ignoreCase = true) ||
                        user.username.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isGroupMode) "Thêm thành viên" else "Tin nhắn mới",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedUserIds.isNotEmpty()) {
                            Text(
                                "Đã chọn ${selectedUserIds.size} người",
                                fontSize = 12.sp,
                                color = TealPrimary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isGroupMode || selectedUserIds.isNotEmpty()) {
                            isGroupMode = false
                            selectedUserIds = emptySet()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(if (isGroupMode) Icons.Default.Close else Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (selectedUserIds.isNotEmpty()) {
                        TextButton(onClick = { showGroupNameDialog = true }) {
                            Text("Tiếp tục", color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Thanh tìm kiếm (Lọc local)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm trong danh sách bạn bè...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Quick Actions (Chỉ hiện khi không tìm kiếm và không chọn nhóm)
            if (searchQuery.isBlank() && !isGroupMode) {
                QuickActionButtons(
                    onCreateGroup = { isGroupMode = true },
                    onAddContact = onAddContact
                )
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (displayedUsers.isEmpty()) {
                    item {
                        if (searchQuery.isBlank()) EmptyContactList() else EmptySearchResult()
                    }
                } else {
                    item {
                        SectionHeader(if (searchQuery.isBlank()) "Bạn bè của bạn" else "Kết quả lọc")
                    }

                    items(displayedUsers) { user ->
                        val isSelected = selectedUserIds.contains(user.id)

                        NewMessageContactRow(
                            contact = user,
                            isSelected = isSelected,
                            onClick = {
                                if (isGroupMode || isSelected) {
                                    // Chế độ chọn thành viên nhóm
                                    selectedUserIds = if (isSelected) {
                                        selectedUserIds - user.id
                                    } else {
                                        selectedUserIds + user.id
                                    }
                                } else {
                                    // Chế độ nhắn tin 1-1
                                    onUserSelected(user)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog nhập tên nhóm
    if (showGroupNameDialog) {
        AlertDialog(
            onDismissRequest = { showGroupNameDialog = false },
            title = { Text("Tên nhóm mới") },
            text = {
                Column {
                    Text("Thành viên đã chọn: ${selectedUserIds.size}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = { Text("Nhập tên nhóm...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        chatViewModel.createNewGroup(
                            name = groupName.ifBlank { "Nhóm mới" },
                            selectedMemberIds = selectedUserIds.toList()
                        )
                        showGroupNameDialog = false
                        onBack()
                    },
                    enabled = groupName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Tạo ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroupNameDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TealPrimary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun NewMessageContactRow(
    contact: User,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 🛠️ THÊM LOGIC GIẢI MÃ BASE64 TƯƠNG TỰ MÀN PROFILE
    val avatarUrl = contact.avatarUrl
    val imageModel = remember(avatarUrl) {
        if (!avatarUrl.isNullOrBlank() && avatarUrl.startsWith("data:image")) {
            try {
                // Tách bỏ tiền tố và giải mã sang ByteArray
                val base64String = avatarUrl.substringAfter(",")
                android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                avatarUrl
            }
        } else {
            avatarUrl
        }
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) TealVeryLight else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = TealLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // ✅ SỬ DỤNG imageModel ĐÃ GIẢI MÃ
                    if (!avatarUrl.isNullOrBlank() && avatarUrl != DEFAULT_AVATAR_NEW_MESSAGE) {
                        AsyncImage(
                            model = imageModel, // <--- Cập nhật ở đây
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            onError = {
                                android.util.Log.e("COIL_ERROR", "Lỗi load ảnh tại NewMessage cho: ${contact.username}")
                            }
                        )
                    } else {
                        val displayName = contact.fullName.ifBlank { contact.username }
                        Text(
                            text = displayName.trim().take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    contact.fullName.ifBlank { contact.username },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text("@${contact.username}", fontSize = 12.sp, color = Color.Gray)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = TealPrimary, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun QuickActionButtons(onCreateGroup: () -> Unit, onAddContact: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onCreateGroup() }
        ) {
            Surface(Modifier.size(50.dp), CircleShape, color = TealVeryLight) {
                Icon(Icons.Default.GroupAdd, null, Modifier.padding(12.dp), tint = TealPrimary)
            }
            Text("Tạo nhóm", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onAddContact() }
        ) {
            Surface(Modifier.size(50.dp), CircleShape, color = TealVeryLight) {
                Icon(Icons.Default.PersonAdd, null, Modifier.padding(12.dp), tint = TealPrimary)
            }
            Text("Thêm bạn", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun EmptySearchResult() {
    Text(
        "Không tìm thấy bạn bè nào khớp với từ khóa",
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        textAlign = TextAlign.Center,
        color = Color.Gray
    )
}

@Composable
fun EmptyContactList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.PeopleOutline, null, Modifier.size(48.dp), tint = Color.LightGray)
        Spacer(Modifier.height(8.dp))
        Text(
            "Danh sách bạn bè trống",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}