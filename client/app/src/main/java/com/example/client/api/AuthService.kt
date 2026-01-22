package com.example.client.api

import androidx.compose.ui.graphics.vector.Path
import com.example.client.model.data.Message
import com.example.client.models.LoginRequest
import com.example.client.models.LoginResponse
import com.example.client.models.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {
    // Gọi API: https://chat-app-0dv5.onrender.com/api/auth/login
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Gọi API: https://chat-app-0dv5.onrender.com/api/auth/register
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>
    @GET("api/messages/{roomId}")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: String
    ): List<Message>

}