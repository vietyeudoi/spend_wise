package com.example.spendwise.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index(
            value = [
                "categoryId",
                "month",
                "year"
            ],
            unique = true
        )
    ]
)
data class Budget(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val categoryId: Int,

    val limitAmount: Double,

    val month: Int,

    val year: Int
)
