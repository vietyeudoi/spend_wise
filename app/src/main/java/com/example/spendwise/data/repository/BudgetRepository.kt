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

    fun insert(budget: Budget) {
        executor.execute { dao.insert(budget) }
    }

    fun update(budget: Budget) {
        executor.execute { dao.update(budget) }
    }

    fun delete(budget: Budget) {
        executor.execute { dao.delete(budget) }
    }

    // Dùng để kiểm tra ngân sách trước khi thêm/sửa (chạy ngoài main thread)
    fun getByCategoryAndMonth(categoryId: Int, month: Int, year: Int): Budget? =
        dao.getByCategoryAndMonth(categoryId, month, year)

    fun getLimitAmount(categoryId: Int, month: Int, year: Int): Double? =
        dao.getLimitAmount(categoryId, month, year)
}
