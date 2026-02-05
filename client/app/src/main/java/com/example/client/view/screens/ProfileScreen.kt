package com.example.client.view.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.* // Đảm bảo đã import AlertDialog, OutlinedTextField, TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.api.AuthService // Import AuthService
import com.example.client.api.RetrofitClient // Import RetrofitClient
import com.example.client.models.UpdateProfileRequest // Import UpdateProfileRequest
import com.example.client.view.theme.TealPrimary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE)

    // Sử dụng mutableStateOf để tên có thể thay đổi trên UI
    var currentUsername by remember {
        mutableStateOf(sharedPref.getString("USERNAME", "Unknown User") ?: "User")
    }

    // State để điều khiển việc hiển thị dialog sửa tên
    var showEditNameDialog by remember { mutableStateOf(false) }
    // State để lưu giá trị tên mới khi đang nhập trong dialog
    var newNameInput by remember { mutableStateOf(currentUsername) }

    // --- DIALOG SỬA TÊN ---
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false }, // Đóng dialog khi click ra ngoài
            title = { Text("Chỉnh sửa tên hiển thị") },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("Tên mới") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // Kiểm tra nếu tên mới không rỗng và khác tên cũ
                    if (newNameInput.isNotBlank() && newNameInput != currentUsername) {
                        // GỌI HÀM CẬP NHẬT TÊN LÊN SERVER
                        updateUserName(
                            context = context,
                            newName = newNameInput,
                            onSuccess = {
                                // Cập nhật tên trên UI và SharedPreferences nếu thành công
                                currentUsername = newNameInput
                                sharedPref.edit().putString("USERNAME", newNameInput).apply()
                                showEditNameDialog = false // Đóng dialog
                                Toast.makeText(context, "Đổi tên thành công!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        showEditNameDialog = false // Đóng dialog nếu không có thay đổi hoặc rỗng
                    }
                }) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang cá nhân", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Hiển thị chữ cái đầu tiên của tên hiện tại
                Text(text = currentUsername.take(1).uppercase(), fontSize = 40.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Xin chào,", fontSize = 18.sp, color = Color.Gray)
            Text(text = currentUsername, fontSize = 28.sp, fontWeight = FontWeight.Bold) // Hiển thị tên từ State

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    newNameInput = currentUsername // Đặt giá trị mặc định cho input khi mở dialog
                    showEditNameDialog = true // Mở dialog khi bấm nút "Chỉnh sửa thông tin"
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Chỉnh sửa thông tin")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    try {
                        com.example.client.api.SocketHandler.closeConnection()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    with(sharedPref.edit()) {
                        clear()
                        apply()
                    }
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(" ĐĂNG XUẤT", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Hàm gọi API để cập nhật tên người dùng
fun updateUserName(context: Context, newName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    val authService = RetrofitClient.instance.create(AuthService::class.java)
    val token = context.getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE).getString("JWT_TOKEN", null)

    if (token == null) {
        onFailure("Bạn chưa đăng nhập.")
        return
    }

    // Đảm bảo UpdateProfileRequest của bạn có trường "fullName"
    // Nếu server mong đợi tên trường khác (ví dụ: "name"), hãy thay đổi data class và Map tương ứng.
    val requestBody = UpdateProfileRequest(fullName = newName)

    authService.updateProfile("Bearer $token", requestBody).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                onSuccess()
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileScreen", "Update failed: ${response.code()} - $errorBody")
                onFailure("Cập nhật thất bại: ${response.message()} - ${errorBody ?: ""}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("ProfileScreen", "Update error: ${t.message}", t)
            onFailure("Lỗi kết nối: ${t.message}")
        }
    })
}