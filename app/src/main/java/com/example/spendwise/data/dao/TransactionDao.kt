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
    //repos gọi
    //Thêm giao dịch
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    //Insert Là annotation của Room dùng để thêm dữ liệu vào bảng.
    //REPLACE: Nếu khóa chính bị trùng thì dữ liệu cũ sẽ bị ghi đè.
    fun insert(transaction: Transaction)

    @Update
    fun update(transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    // Tất cả giao dịch, mới nhất trước
    //repos gọi
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): LiveData<List<Transaction>>

    // 10 giao dịch gần nhất (dùng cho màn hình Dashboard)
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 10")
    fun getRecent(): LiveData<List<Transaction>>

    // Lấy một giao dịch theo id (ĐÃ SỬA: Bọc LiveData để đồng bộ với ViewModel và UI)
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getById(id: Int): LiveData<Transaction?>
    // Lấy theo ngày cụ thể (định dạng yyyy-MM-dd)
    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%Y-%m-%d', date / 1000, 'unixepoch') = :date
        ORDER BY date DESC
    """)
    fun getByDate(date: String): LiveData<List<Transaction>>

    // Lấy theo tháng và năm
    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%m', date / 1000, 'unixepoch') = printf('%02d', :month)
        AND   strftime('%Y', date / 1000, 'unixepoch') = :year
        ORDER BY date DESC
    """)
    fun getByMonth(month: Int, year: String): LiveData<List<Transaction>>

    // Lấy theo năm
    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year
        ORDER BY date DESC
    """)
    fun getByYear(year: String): LiveData<List<Transaction>>

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

    @Query("""
        SELECT t.categoryId as categoryId, c.name as categoryName, SUM(t.amount) as total
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'expense'
        AND strftime('%m', t.date / 1000, 'unixepoch') = printf('%02d', :month)
        AND strftime('%Y', t.date / 1000, 'unixepoch') = :year
        GROUP BY t.categoryId
        ORDER BY total DESC
    """)
    fun getSpendingByCategorySync(month: Int, year: String): List<CategoryTotal>

    // Tìm kiếm theo tên giao dịch
    @Query("""
    SELECT t.* FROM transactions t
    LEFT JOIN categories c ON t.categoryId = c.id
    WHERE t.title LIKE '%' || :keyword || '%'
       OR t.note  LIKE '%' || :keyword || '%'
       OR c.name  LIKE '%' || :keyword || '%'
    ORDER BY t.date DESC
""")
    //repos gọi
    fun search(keyword: String): LiveData<List<Transaction>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM transactions 
        WHERE type = 'expense' 
        AND date >= :startOfDay 
        AND date <= :endOfDay
    """)
    fun getTodayExpense(startOfDay: Long, endOfDay: Long): LiveData<Double>

    @Query("""
    SELECT t.categoryId,
           c.name AS categoryName,
           SUM(t.amount) AS total
    FROM transactions t
    INNER JOIN categories c ON t.categoryId = c.id
    WHERE t.type = 'expense'
      AND strftime('%Y-%m-%d', t.date / 1000, 'unixepoch') = :date
    GROUP BY t.categoryId
    ORDER BY total DESC
""")
    fun getSpendingByDate(date: String): LiveData<List<CategoryTotal>>

    @Query("""
    SELECT t.categoryId,
           c.name AS categoryName,
           SUM(t.amount) AS total
    FROM transactions t
    INNER JOIN categories c ON t.categoryId = c.id
    WHERE t.type = 'expense'
      AND strftime('%Y', t.date / 1000, 'unixepoch') = :year
    GROUP BY t.categoryId
    ORDER BY total DESC
""")
    fun getSpendingByYear(year: String): LiveData<List<CategoryTotal>>

    @Query("""
    SELECT t.categoryId,
           c.name AS categoryName,
           SUM(t.amount) AS total
    FROM transactions t
    INNER JOIN categories c ON t.categoryId = c.id
    WHERE t.type = 'expense'
    GROUP BY t.categoryId
    ORDER BY total DESC
""")
    fun getAllSpendingByCategory(): LiveData<List<CategoryTotal>>
}

// Data class nhỏ để nhận kết quả query tổng theo danh mục
data class CategoryTotal(
    val categoryId: Int?,
    val categoryName: String,
    val total: Double
)