package com.example.spendwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.spendwise.data.dao.CategoryTotal
import com.example.spendwise.data.entity.Transaction
import com.example.spendwise.data.repository.CategoryRepository
import com.example.spendwise.data.repository.TransactionRepository
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepo = TransactionRepository(application)
    private val categoryRepo    = CategoryRepository(application)

    // Lấy tháng/năm hiện tại tự động
    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH) + 1
    private val currentYear  = calendar.get(Calendar.YEAR).toString()

    // ── LiveData dùng trong Fragment/Activity ────────────────────────────────

    val recentTransactions: LiveData<List<Transaction>> = transactionRepo.getRecent()

    val monthlyTransactions: LiveData<List<Transaction>> =
        transactionRepo.getByMonth(currentMonth, currentYear)

    val totalIncome: LiveData<Double> =
        transactionRepo.getTotalByTypeAndMonth("income", currentMonth, currentYear)

    val totalExpense: LiveData<Double> =
        transactionRepo.getTotalByTypeAndMonth("expense", currentMonth, currentYear)

    val categorySpending: LiveData<List<CategoryTotal>> =
        transactionRepo.getSpendingByCategory(currentMonth, currentYear)

    val allCategories = categoryRepo.getAll()

    // ── Thao tác dữ liệu ────────────────────────────────────────────────────

    fun insert(transaction: Transaction) = transactionRepo.insert(transaction)

    fun update(transaction: Transaction) = transactionRepo.update(transaction)

    fun delete(transaction: Transaction) = transactionRepo.delete(transaction)

    fun search(keyword: String): LiveData<List<Transaction>> =
        transactionRepo.search(keyword)
}
