package com.example.client.api

import com.example.client.model.data.Message
import com.example.client.models.LoginRequest
import com.example.client.models.LoginResponse
import com.example.client.models.RegisterRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthService {
    // Gọi API: https://chat-app-0dv5.onrender.com/api/auth/login
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Gọi API: https://chat-app-0dv5.onrender.com/api/auth/register
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>
    @Multipart
    @POST("api/upload") // Đảm bảo server bạn có route này (như hướng dẫn trước)
    suspend fun uploadFile(@Part file: MultipartBody.Part): UploadResponse

    @GET("api/messages/search")
    suspend fun searchMessages(
        @Query("roomId") roomId: String,
        @Query("keyword") keyword: String
    ): List<Message>
}
data class UploadResponse(val url: String, val fileName: String)