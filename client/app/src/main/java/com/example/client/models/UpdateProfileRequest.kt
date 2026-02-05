package com.example.client.models

data class UpdateProfileRequest(
    val fullName: String,
    val avatarUrl: String? = null// Tên trường này phải khớp với tên trường mà Server mong đợi
)