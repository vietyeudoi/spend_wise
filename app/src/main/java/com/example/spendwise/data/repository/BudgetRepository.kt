package com.example.spendwise.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.spendwise.data.dao.BudgetDao
import com.example.spendwise.data.database.AppDatabase
import com.example.spendwise.data.entity.Budget
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BudgetRepository(application: Application) {

    private val dao: BudgetDao
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = AppDatabase.getInstance(application)
        dao = db.budgetDao()
    }

    fun getByMonth(month: Int, year: Int): LiveData<List<Budget>> =
        dao.getByMonth(month, year)

    suspend fun insert(budget: Budget) {
        dao.insert(budget)
    }

    suspend fun update(budget: Budget) {
        dao.update(budget)
    }

    suspend fun delete(budget: Budget) {
        dao.delete(budget)
    }
    // Dùng để kiểm tra ngân sách trước khi thêm/sửa (chạy ngoài main thread)
    fun getByCategoryAndMonth(categoryId: Int, month: Int, year: Int): Budget? =
        dao.getByCategoryAndMonth(categoryId, month, year)

    fun getLimitAmount(categoryId: Int, month: Int, year: Int): Double? =
        dao.getLimitAmount(categoryId, month, year)
}
