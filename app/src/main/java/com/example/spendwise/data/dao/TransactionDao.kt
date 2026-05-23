package com.example.spendwise.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.spendwise.data.entity.Transaction

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(transaction: Transaction)

    @Update
    fun update(transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    // Tất cả giao dịch, mới nhất trước
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): LiveData<List<Transaction>>

    // 10 giao dịch gần nhất (dùng cho màn hình Dashboard)
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 10")
    fun getRecent(): LiveData<List<Transaction>>

    // Lấy theo tháng và năm
    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%m', date / 1000, 'unixepoch') = printf('%02d', :month)
        AND   strftime('%Y', date / 1000, 'unixepoch') = :year
        ORDER BY date DESC
    """)
    fun getByMonth(month: Int, year: String): LiveData<List<Transaction>>

    // Tổng thu hoặc chi trong tháng
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = :type
        AND   strftime('%m', date / 1000, 'unixepoch') = printf('%02d', :month)
        AND   strftime('%Y', date / 1000, 'unixepoch') = :year
    """)
    fun getTotalByTypeAndMonth(type: String, month: Int, year: String): LiveData<Double>

    // Tổng chi theo từng danh mục trong tháng (dùng cho biểu đồ)
    @Query("""
    SELECT t.categoryId,
           c.name AS categoryName,
           SUM(t.amount) AS total
    FROM transactions t
    INNER JOIN categories c ON t.categoryId = c.id
    WHERE t.type = 'expense'
    AND strftime('%m', t.date / 1000, 'unixepoch') = printf('%02d', :month)
    AND strftime('%Y', t.date / 1000, 'unixepoch') = :year
    GROUP BY t.categoryId
    ORDER BY total DESC
""")
    fun getSpendingByCategory(month: Int, year: String): LiveData<List<CategoryTotal>>

    // Tìm kiếm theo tên giao dịch
    @Query("SELECT * FROM transactions WHERE title LIKE '%' || :keyword || '%' ORDER BY date DESC")
    fun search(keyword: String): LiveData<List<Transaction>>

    // Lấy một giao dịch theo id (dùng khi sửa)
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Int): Transaction?
}

// Data class nhỏ để nhận kết quả query tổng theo danh mục
data class CategoryTotal(
    val categoryId: Int?,
    val categoryName: String,   // ← thêm field này
    val total: Double
)