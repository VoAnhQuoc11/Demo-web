package com.example.client.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 👇 LINK SERVER CỦA AN (Đừng dùng localhost)
    private const val BASE_URL = "http://192.168.1.13:3000/"

    // 👇 1. Cấu hình bộ đếm giờ (Timeout)
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Chờ kết nối 60s
        .readTimeout(60, TimeUnit.SECONDS)    // Chờ đọc dữ liệu 60s
        .writeTimeout(60, TimeUnit.SECONDS)   // Chờ gửi dữ liệu 60s
        .build()

    val instance: Retrofit by lazy { // <-- Đảm bảo kiểu là 'Retrofit'
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // KHÔNG CÓ DÒNG NÀY: .create(AuthService::class.java)
        // Nếu có, hãy xóa hoặc comment nó đi!
    }
}