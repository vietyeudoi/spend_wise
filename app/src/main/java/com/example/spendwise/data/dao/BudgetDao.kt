package com.example.spendwise.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.spendwise.data.entity.Budget

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    // Tất cả ngân sách trong tháng
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getByMonth(month: Int, year: Int): LiveData<List<Budget>>

    // Ngân sách của một danh mục trong tháng (dùng khi thêm/sửa)
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year LIMIT 1")
    fun getByCategoryAndMonth(categoryId: Int, month: Int, year: Int): Budget?

    // Hạn mức của một danh mục (dùng để tính % đã dùng)
    @Query("SELECT limitAmount FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year")
    fun getLimitAmount(categoryId: Int, month: Int, year: Int): Double?

}
