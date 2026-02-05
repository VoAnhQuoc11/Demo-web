package com.example.client.view.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.client.api.AuthService
import com.example.client.api.RetrofitClient
import com.example.client.models.UpdateProfileRequest
import com.example.client.view.theme.TealPrimary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE)

    var currentUsername by remember {
        mutableStateOf(sharedPref.getString("USERNAME", "Unknown User") ?: "User")
    }

    // State lưu Avatar URL hiện tại (có thể là link hoặc base64 từ server)
    var currentAvatarUrl by remember {
        mutableStateOf(sharedPref.getString("AVATAR_URL", "") ?: "")
    }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(currentUsername) }

    // Launcher để chọn ảnh từ thư viện thiết bị
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64Image = uriToBase64Png(context, it)
            if (base64Image != null) {
                // Gọi API cập nhật ảnh ngay khi chọn xong
                updateUserProfile(
                    context = context,
                    newName = currentUsername,
                    newAvatar = base64Image,
                    onSuccess = {
                        currentAvatarUrl = base64Image
                        sharedPref.edit().putString("AVATAR_URL", base64Image).apply()
                        Toast.makeText(context, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
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
                    if (newNameInput.isNotBlank() && newNameInput != currentUsername) {
                        updateUserProfile(
                            context = context,
                            newName = newNameInput,
                            newAvatar = null, // Giữ nguyên ảnh cũ
                            onSuccess = {
                                currentUsername = newNameInput
                                sharedPref.edit().putString("USERNAME", newNameInput).apply()
                                showEditNameDialog = false
                                Toast.makeText(context, "Đổi tên thành công!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        showEditNameDialog = false
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

            // PHẦN AVATAR CÓ NÚT CHỈNH SỬA
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { imagePickerLauncher.launch("image/*") }, // Click vào ảnh để đổi
                contentAlignment = Alignment.Center
            ) {
                if (currentAvatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = currentAvatarUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, TealPrimary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentUsername.take(1).uppercase(), fontSize = 40.sp, color = Color.White)
                    }
                }

                // Icon cái bút để người dùng biết là sửa được
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = TealPrimary,
                    tonalElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Avatar",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Xin chào,", fontSize = 18.sp, color = Color.Gray)
            Text(text = currentUsername, fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    newNameInput = currentUsername
                    showEditNameDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Chỉnh sửa tên hiển thị")
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

/**
 * Hàm hỗ trợ chuyển đổi URI ảnh sang chuỗi Base64 định dạng PNG
 */
fun uriToBase64Png(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        // Bước 1: Giảm kích thước ảnh (Scale) để tiết kiệm dung lượng DB
        val scaledBitmap = Bitmap.createScaledBitmap(
            originalBitmap, 400, 400, true // Resize về 400x400 px
        )

        val outputStream = ByteArrayOutputStream()
        // Bước 2: Nén định dạng PNG
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        val byteArray = outputStream.toByteArray()

        "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Lỗi convert ảnh: ${e.message}")
        null
    }
}

// Trong hàm updateUserProfile, hãy Log dữ liệu ra để kiểm tra
fun updateUserProfile(context: Context, newName: String, newAvatar: String?, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    val authService = RetrofitClient.instance.create(AuthService::class.java)
    val sharedPref = context.getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("TOKEN", null)

    val requestBody = UpdateProfileRequest(fullName = newName, avatarUrl = newAvatar)

    // Log để kiểm tra xem Client có thực sự gửi chuỗi ảnh đi không
    Log.d("API_DEBUG", "Gửi Avatar: ${newAvatar?.take(50)}...")

    authService.updateProfile("Bearer $token", requestBody).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                onSuccess()
            } else {
                // Nếu lỗi 413 tức là ảnh vẫn quá lớn, cần tăng limit ở Server (Bước 1)
                Log.e("API_ERROR", "Mã lỗi: ${response.code()}")
                onFailure("Lỗi Server: ${response.code()}")
            }
        }
        override fun onFailure(call: Call<Void>, t: Throwable) {
            onFailure("Lỗi kết nối: ${t.message}")
        }
    })
}