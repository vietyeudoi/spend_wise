package com.example.spendwise.utils

import androidx.compose.ui.graphics.Color

fun isOverBudget(spent: Double, limit: Double): Boolean = spent >= limit * 0.8
// Hàm kiểm tra xem chi tiêu (spent) đã vượt ngưỡng ngân sách chưa
// Điều kiện: nếu chi tiêu >= 80% của hạn mức (limit) thì trả về true (đã vượt ngưỡng cảnh báo)
val NormalContainerColor = Color(0xFFE8F5EE)
val NormalTextColor = Color(0xFF085041)
val OverContainerColor = Color(0xFFFDE8E8) // đỏ nhạt
val OverTextColor = Color(0xFFB00020)
