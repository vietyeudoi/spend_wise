package com.example.spendwise.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.spendwise.data.dao.CategoryDao
import com.example.spendwise.data.database.AppDatabase
import com.example.spendwise.data.entity.Category
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CategoryRepository private constructor(application: Application) {

    private val dao: CategoryDao
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    init {
        val db = AppDatabase.getInstance(application)
        dao = db.categoryDao()
        // Tự động thêm danh mục mặc định nếu chưa có — chỉ chạy 1 lần duy nhất
        // vì CategoryRepository giờ là singleton (xem companion object bên dưới)
        executor.execute { seedIfEmpty() }
    }

    fun getAll(): LiveData<List<Category>> = dao.getAll()

    fun getByType(type: String): LiveData<List<Category>> = dao.getByType(type)

    fun insert(category: Category) {
        executor.execute { dao.insert(category) }
    }

    // Insert kèm callback, kết quả trả về trên main thread (an toàn cho Toast/Compose state)
    fun insert(category: Category, onComplete: (Long) -> Unit) {
        executor.execute {
            val id = dao.insert(category)
            mainHandler.post { onComplete(id) }
        }
    }

    fun update(category: Category) {
        executor.execute { dao.update(category) }
    }

    fun delete(category: Category) {
        executor.execute { dao.delete(category) }
    }

    // Seed danh mục mặc định lần đầu cài app
    private fun seedIfEmpty() {
        val existing = dao.getById(1)
        if (existing == null) {
            dao.insertAll(DEFAULT_CATEGORIES)
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: CategoryRepository? = null

        // ── Singleton: mọi ViewModel dùng chung 1 instance duy nhất ───────────────
        fun getInstance(application: Application): CategoryRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CategoryRepository(application).also { INSTANCE = it }
            }

        private val DEFAULT_CATEGORIES = listOf(
            Category(name = "Ăn uống", icon = "ic_food", type = "expense"),
            Category(name = "Đi lại", icon = "ic_transport", type = "expense"),
            Category(name = "Mua sắm", icon = "ic_shopping", type = "expense"),
            Category(name = "Giải trí", icon = "ic_entertainment", type = "expense"),
            Category(name = "Y tế", icon = "ic_health", type = "expense"),
            Category(name = "Giáo dục", icon = "ic_education", type = "expense"),
            Category(name = "Hóa đơn", icon = "ic_bill", type = "expense"),
            Category(name = "Khác", icon = "ic_other", type = "expense"),
            Category(name = "Lương", icon = "ic_salary", type = "income"),
            Category(name = "Làm thêm", icon = "ic_freelance", type = "income"),
            Category(name = "Quà tặng", icon = "ic_gift", type = "income"),
        )
    }
}