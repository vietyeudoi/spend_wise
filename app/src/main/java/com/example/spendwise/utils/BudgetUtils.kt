package com.example.spendwise.utils

import androidx.compose.ui.graphics.Color

fun isOverBudget(spent: Double, limit: Double): Boolean = spent >= limit * 0.8

val NormalContainerColor = Color(0xFFE8F5EE)
val NormalTextColor = Color(0xFF085041)
val OverContainerColor = Color(0xFFFDE8E8) // đỏ nhạt
val OverTextColor = Color(0xFFB00020)
