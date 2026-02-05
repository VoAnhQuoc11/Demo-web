package com.example.client.models

data class UpdateProfileRequest(
    val fullName: String // Tên trường này phải khớp với tên trường mà Server mong đợi
)