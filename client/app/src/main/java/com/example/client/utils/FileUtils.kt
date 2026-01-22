// client/app/src/main/java/com/example/client/utils/FileUtils.kt
package com.example.client.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    // Hàm lấy File từ Uri
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver

            // 1. Lấy tên file gốc
            val fileName = getFileName(context, uri)

            // 2. Tạo một file tạm trong thư mục cache của app
            val tempFile = File(context.cacheDir, fileName)

            // 3. Copy dữ liệu từ Uri vào file tạm
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            tempFile // Trả về file tạm đã có dữ liệu
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Hàm phụ trợ để lấy tên file
    private fun getFileName(context: Context, uri: Uri): String {
        var name = "temp_file"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
}