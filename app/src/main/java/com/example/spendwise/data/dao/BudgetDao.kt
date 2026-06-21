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


    // Thêm ngân sách
    // Nếu trùng category + month + year thì cập nhật
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insert(
        budget: Budget
    )


    // Sửa ngân sách
    @Update
    suspend fun update(
        budget: Budget
    )


    // Xóa ngân sách
    @Delete
    suspend fun delete(
        budget: Budget
    )


    /*
        Lấy ngân sách của tháng đang chọn

        Ví dụ:
        tháng 6 năm 2026

        chỉ trả:
        - Ăn uống 6/2026
        - Đi lại 6/2026

        Không lấy tháng khác
     */
    @Query(
        """
        SELECT *
        FROM budgets
        WHERE month = :month
        AND year = :year
        ORDER BY id DESC
        """
    )
    fun getByMonth(
        month: Int,
        year: Int
    ): LiveData<List<Budget>>

    @Query(
        """
        SELECT *
        FROM budgets
        WHERE month = :month
        AND year = :year
        ORDER BY id DESC
        """
    )
    fun getByMonthSync(
        month: Int,
        year: Int
    ): List<Budget>



    /*
        Kiểm tra danh mục đã có ngân sách
        trong tháng đó chưa

        Ví dụ:
        Ăn uống - 6/2026

        tồn tại -> trả về
        chưa có -> null
     */
    @Query(
        """
        SELECT *
        FROM budgets
        WHERE categoryId = :categoryId
        AND month = :month
        AND year = :year
        LIMIT 1
        """
    )
    suspend fun getByCategoryAndMonth(
        categoryId: Int,
        month: Int,
        year: Int
    ): Budget?



    /*
        Lấy số tiền giới hạn của danh mục
     */
    @Query(
        """
        SELECT limitAmount
        FROM budgets
        WHERE categoryId = :categoryId
        AND month = :month
        AND year = :year
        LIMIT 1
        """
    )
    suspend fun getLimitAmount(
        categoryId: Int,
        month: Int,
        year: Int
    ): Double?



    /*
        Lấy toàn bộ ngân sách
        dùng khi thống kê
     */
    @Query(
        """
        SELECT *
        FROM budgets
        ORDER BY year DESC, month DESC
        """
    )
    fun getAll(): LiveData<List<Budget>>



    /*
        Xóa toàn bộ ngân sách của 1 tháng
        ví dụ reset tháng
     */
    @Query(
        """
        DELETE FROM budgets
        WHERE month = :month
        AND year = :year
        """
    )
    suspend fun deleteByMonth(
        month: Int,
        year: Int
    )

    @Query("SELECT COALESCE(SUM(limitAmount), 0.0) FROM budgets WHERE month = :month AND year = :year")
    fun getTotalBudgetLimit(month: Int, year: Int): LiveData<Double>
}