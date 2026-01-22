package com.example.client.view.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack // [MỚI] Icon quay lại
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search // [MỚI] Icon tìm kiếm
import androidx.compose.material.icons.filled.Close // [MỚI] Icon đóng tìm kiếm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.client.view.components.MessageBubble
import com.example.client.viewmodel.ChatViewModel
import com.example.client.view.theme.YellowPrimary
import com.example.client.view.theme.TextBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current

    // --- STATE TỪ VIEWMODEL ---
    val messages by viewModel.messages.collectAsState()
    val typingUser by viewModel.typingUser.collectAsState()

    // [MỚI] State cho tìm kiếm
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var searchQuery by remember { mutableStateOf("") } // Text trong ô tìm kiếm

    // State input tin nhắn thường
    var textState by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // --- LOGIC LAUNCHER (GIỮ NGUYÊN) ---
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.sendImage(context, uri)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAndSendFile(context, it) }
    }

    // Join room khi mở màn hình
    LaunchedEffect(Unit) {
        viewModel.joinRoom(viewModel.currentRoomId)
    }

    // Tự động cuộn xuống dưới khi có tin nhắn mới (chỉ khi KHÔNG tìm kiếm)
    LaunchedEffect(messages.size, isSearching) {
        if (!isSearching && messages.isNotEmpty()) {
            val lastIndex = messages.size - 1
            if (messages.size > 10 && listState.firstVisibleItemIndex < 2) {
                listState.scrollToItem(lastIndex)
            } else {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    // --- XÁC ĐỊNH DANH SÁCH HIỂN THỊ ---
    // Nếu đang tìm kiếm và có kết quả -> Hiện kết quả. Ngược lại hiện tin nhắn thường.
    val listToDisplay = if (isSearching && searchQuery.isNotEmpty()) searchResults else messages

    Scaffold(
        topBar = {
            if (isSearching) {
                // --- GIAO DIỆN THANH TÌM KIẾM ---
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchMessages(it) // Gọi hàm tìm kiếm trong ViewModel
                            },
                            placeholder = { Text("Nhập nội dung tìm...", fontSize = 14.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            // Tắt tìm kiếm
                            viewModel.toggleSearchMode()
                            searchQuery = "" // Xóa text tìm kiếm
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = ""; viewModel.searchMessages("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = YellowPrimary)
                )
            } else {
                // --- GIAO DIỆN TITLE BÌNH THƯỜNG ---
                TopAppBar(
                    title = { Text("Chat Room", color = TextBlack, fontWeight = FontWeight.Bold) },
                    actions = {
                        // Nút bật chế độ tìm kiếm
                        IconButton(onClick = { viewModel.toggleSearchMode() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextBlack)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = YellowPrimary)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // --- DANH SÁCH TIN NHẮN ---
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                state = listState,
                reverseLayout = false // Chú ý: Chat thường reverse=true, nhưng code của bạn đang để false
            ) {
                items(listToDisplay) { message ->
                    MessageBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        onSeen = {
                            // Chỉ đánh dấu đã xem khi KHÔNG đang tìm kiếm
                            if (!isSearching) viewModel.markAsSeen(message)
                        }
                    )
                }

                if (isSearching && listToDisplay.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Text(
                            "Không tìm thấy kết quả nào.",
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // --- KHU VỰC NHẬP TIN NHẮN (Ẩn khi đang tìm kiếm) ---
            if (!isSearching) {
                // Hiển thị người đang gõ
                if (typingUser != null) {
                    Text(
                        text = "Người khác đang soạn tin...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút gửi ảnh
                    IconButton(onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Gửi ảnh", tint = YellowPrimary)
                    }

                    // Nút gửi file
                    IconButton(onClick = {
                        val mimeTypes = arrayOf(
                            "application/pdf",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                        filePickerLauncher.launch(mimeTypes)
                    }) {
                        Icon(Icons.Default.Description, contentDescription = "Send File")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Ô nhập tin nhắn
                    OutlinedTextField(
                        value = textState,
                        onValueChange = {
                            textState = it
                            viewModel.onUserInputChanged(it)
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nhập tin nhắn...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YellowPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Nút gửi text
                    Button(
                        onClick = {
                            if (textState.isNotBlank()) {
                                viewModel.sendMessage(textState)
                                textState = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowPrimary,
                            contentColor = TextBlack
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("Gửi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}