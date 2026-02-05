package com.example.client.api

import com.example.client.models.LoginRequest
import com.example.client.models.LoginResponse
import com.example.client.models.RegisterRequest
import com.example.client.models.UpdateProfileRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthService {
    // Gọi API: https://chat-app-0dv5.onrender.com/api/auth/login
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>


    // Gọi API: https://chat-app-0dv5.onrender.com/api/auth/register
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>
    // Thêm vào file D:/XDOOP/Demo-web/client/app/src/main/java/com/example/client/api/AuthService.kt

    @PUT("api/users/update")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<Void>
}