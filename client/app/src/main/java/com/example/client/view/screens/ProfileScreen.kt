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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE) }

    // State đồng bộ từ SharedPreferences
    var currentUsername by remember {
        mutableStateOf(sharedPref.getString("USERNAME", "Unknown User") ?: "User")
    }

    var currentAvatarUrl by remember {
        mutableStateOf(sharedPref.getString("AVATAR_URL", "") ?: "")
    }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(currentUsername) }

    // Kiểm tra xem URL có phải là ảnh hợp lệ để hiển thị không
    // Loại trừ trường hợp rỗng hoặc link mặc định của Imgur
    val isAvatarValid = remember(currentAvatarUrl) {
        currentAvatarUrl.isNotEmpty() && currentAvatarUrl != "https://i.imgur.com/6VBx3io.png"
    }

    // Tối ưu hóa Model cho AsyncImage
    val imageModel = remember(currentAvatarUrl) {
        if (currentAvatarUrl.startsWith("data:image")) {
            try {
                val base64String = currentAvatarUrl.substringAfter(",")
                Base64.decode(base64String, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("COIL_DEBUG", "Giải mã Base64 thất bại: ${e.message}")
                currentAvatarUrl
            }
        } else {
            currentAvatarUrl
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64Image = uriToBase64Png(context, it)
            if (base64Image != null) {
                val oldAvatar = currentAvatarUrl
                currentAvatarUrl = base64Image

                updateUserProfile(
                    context = context,
                    newName = currentUsername,
                    newAvatar = base64Image,
                    onSuccess = {
                        sharedPref.edit().putString("AVATAR_URL", base64Image).apply()
                        Toast.makeText(context, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        currentAvatarUrl = oldAvatar
                        Toast.makeText(context, "Lỗi Server: $err", Toast.LENGTH_LONG).show()
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
                            newAvatar = null,
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

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Logic hiển thị: Nếu avatar hợp lệ thì hiện ảnh, ngược lại hiện chữ cái đầu
                if (isAvatarValid) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, TealPrimary, CircleShape),
                        contentScale = ContentScale.Crop,
                        onError = {
                            Log.e("COIL_ERROR", "Lỗi hiển thị ảnh.")
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .border(2.dp, TealPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUsername.take(1).uppercase(),
                            fontSize = 40.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
                    sharedPref.edit().clear().apply()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("ĐĂNG XUẤT", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun uriToBase64Png(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 400, 400, true)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e("PROFILE_ERROR", "Lỗi convert ảnh: ${e.message}")
        null
    }
}

fun updateUserProfile(
    context: Context,
    newName: String,
    newAvatar: String?,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val authService = RetrofitClient.instance.create(AuthService::class.java)
    val sharedPref = context.getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("TOKEN", null)

    val requestBody = UpdateProfileRequest(fullName = newName, avatarUrl = newAvatar)

    authService.updateProfile("Bearer $token", requestBody).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onFailure("Mã lỗi: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            onFailure(t.message ?: "Lỗi kết nối")
        }
    })
}