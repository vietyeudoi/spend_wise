package com.example.spendwise.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.spendwise.data.dao.CategoryTotal
import com.example.spendwise.data.dao.TransactionDao
import com.example.spendwise.data.database.AppDatabase
import com.example.spendwise.data.entity.Transaction
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TransactionRepository(application: Application) {

    private val dao: TransactionDao
    // ExecutorService để chạy insert/update/delete trên background thread
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = AppDatabase.getInstance(application)
        dao = db.transactionDao()
    }

    // ── Đọc dữ liệu (LiveData tự chạy nền, không cần executor) ───────────────

    fun getAll(): LiveData<List<Transaction>> = dao.getAll()

    fun getRecent(): LiveData<List<Transaction>> = dao.getRecent()

    fun getByMonth(month: Int, year: String): LiveData<List<Transaction>> =
        dao.getByMonth(month, year)

    fun getTotalByTypeAndMonth(type: String, month: Int, year: String): LiveData<Double> =
        dao.getTotalByTypeAndMonth(type, month, year)

    fun getSpendingByCategory(month: Int, year: String): LiveData<List<CategoryTotal>> =
        dao.getSpendingByCategory(month, year)

    fun search(keyword: String): LiveData<List<Transaction>> = dao.search(keyword)

    // ── Ghi dữ liệu (phải chạy trên background thread) ───────────────────────

    fun insert(transaction: Transaction) {
        executor.execute { dao.insert(transaction) }
    }

    fun update(transaction: Transaction) {
        executor.execute { dao.update(transaction) }
    }

    fun delete(transaction: Transaction) {
        executor.execute { dao.delete(transaction) }
    }

    // Dùng khi cần lấy 1 bản ghi để sửa (chạy từ background thread bên ngoài)
    fun getById(id: Int): Transaction? = dao.getById(id)
}
