package com.example.spendwise.data.entity
// Khai báo package chứa entity (bảng dữ liệu) Budget

import androidx.room.Entity // Annotation để đánh dấu class là bảng trong Room
import androidx.room.Index // Annotation để tạo index (chỉ mục) cho bảng
import androidx.room.PrimaryKey // Annotation để đánh dấu khóa chính

@Entity(
    tableName = "budgets", // Tên bảng trong CSDL Room
    indices = [
        Index(
            value = [
                "categoryId", // Cột categoryId
                "month",      // Cột month
                "year"        // Cột year
            ],
            unique = true // Đảm bảo mỗi categoryId + month + year là duy nhất (không trùng)
        )
    ]
)
data class Budget( // Định nghĩa class Budget, mỗi object là một dòng trong bảng "budgets"

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Khóa chính, tự động tăng (id duy nhất cho mỗi ngân sách)

    val categoryId: Int,
    // Id của danh mục (liên kết với bảng Category)

    val limitAmount: Double,
    // Số tiền giới hạn ngân sách cho danh mục đó

    val month: Int,
    // Tháng áp dụng ngân sách (ví dụ: 6)

    val year: Int
    // Năm áp dụng ngân sách (ví dụ: 2026)
)
