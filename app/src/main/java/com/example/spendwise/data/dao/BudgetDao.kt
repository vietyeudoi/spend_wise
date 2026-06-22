package com.example.spendwise.data.dao
// Khai báo package chứa DAO (Data Access Object) cho bảng Budget

import androidx.lifecycle.LiveData // Import LiveData để quan sát dữ liệu
import androidx.room.* // Import các annotation của Room (Dao, Insert, Update, Delete, Query)
import com.example.spendwise.data.entity.Budget // Import entity Budget

@Dao
interface BudgetDao {
    // Định nghĩa interface DAO, chứa các hàm thao tác với bảng Budget trong Room

    // Thêm ngân sách
    // Nếu trùng category + month + year thì cập nhật (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)

    // Sửa ngân sách
    @Update
    suspend fun update(budget: Budget)

    // Xóa ngân sách
    @Delete
    suspend fun delete(budget: Budget)

    /*
        Lấy ngân sách của tháng đang chọn
        Ví dụ: tháng 6 năm 2026
        chỉ trả về các ngân sách thuộc tháng 6/2026
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
    fun getByMonth(month: Int, year: Int): LiveData<List<Budget>>
    // Trả về LiveData danh sách ngân sách theo tháng/năm

    @Query(
        """
        SELECT *
        FROM budgets
        WHERE month = :month
        AND year = :year
        ORDER BY id DESC
        """
    )
    fun getByMonthSync(month: Int, year: Int): List<Budget>
    // Trả về danh sách ngân sách theo tháng/năm (dạng đồng bộ, không LiveData)

    /*
        Kiểm tra danh mục đã có ngân sách trong tháng đó chưa
        Ví dụ: Ăn uống - 6/2026
        Nếu tồn tại -> trả về Budget
        Nếu chưa có -> null
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
    suspend fun getByCategoryAndMonth(categoryId: Int, month: Int, year: Int): Budget?

    /*
        Lấy số tiền giới hạn của một danh mục trong tháng/năm
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
    suspend fun getLimitAmount(categoryId: Int, month: Int, year: Int): Double?

    /*
        Lấy toàn bộ ngân sách (dùng khi thống kê)
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
        Xóa toàn bộ ngân sách của 1 tháng (ví dụ reset tháng)
     */
    @Query(
        """
        DELETE FROM budgets
        WHERE month = :month
        AND year = :year
        """
    )
    suspend fun deleteByMonth(month: Int, year: Int)

    // Lấy tổng hạn mức ngân sách của tháng/năm
    @Query("SELECT COALESCE(SUM(limitAmount), 0.0) FROM budgets WHERE month = :month AND year = :year")
    fun getTotalBudgetLimit(month: Int, year: Int): LiveData<Double>
}
