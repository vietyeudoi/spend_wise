package com.example.spendwise.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.spendwise.data.dao.CategoryTotal
import com.example.spendwise.data.entity.Transaction
import com.example.spendwise.data.entity.Category
import com.example.spendwise.data.repository.CategoryRepository
import com.example.spendwise.data.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.*

class TransactionViewModel(application: Application) :
    AndroidViewModel(application) {

    private val transactionRepo = TransactionRepository(application)
    private val categoryRepo = CategoryRepository.getInstance(application)

    // ================= BUDGET =================
    private val prefs =
        application.getSharedPreferences(
            "spendwise_prefs",
            android.content.Context.MODE_PRIVATE
        )

    private val _budget = mutableDoubleStateOf(
        prefs.getFloat("budget", 0f).toDouble()
    )

    val budget: Double
        get() = _budget.value

    fun setBudget(value: Double) {

        _budget.value = value

        prefs.edit()
            .putFloat(
                "budget",
                value.toFloat()
            )
            .apply()
    }

    // ================= FILTER STATE =================
    private val calendar = Calendar.getInstance()

    var selectedMonth by mutableStateOf(calendar.get(Calendar.MONTH) + 1)
        private set

    var selectedYear by mutableStateOf(calendar.get(Calendar.YEAR))
        private set

    var selectedDate by mutableStateOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
        private set

    var filterMode by mutableStateOf(FilterMode.MONTH)
        private set

    enum class FilterMode {
        DAY, MONTH, YEAR, ALL
    }

    // ================= SET FILTER =================
    //Lọc gd
    fun changeFilterMode(mode: FilterMode) {
        filterMode = mode
    }

    fun nextMonth() {
        if (selectedMonth == 12) {
            selectedMonth = 1
            selectedYear++
        } else selectedMonth++
    }

    fun prevMonth() {
        if (selectedMonth == 1) {
            selectedMonth = 12
            selectedYear--
        } else selectedMonth--
    }

    fun setDate(date: String) {
        selectedDate = date
    }
    fun setMonthYear(month: Int, year: Int) {
        selectedMonth = month
        selectedYear = year
    }

    fun setYear(year: Int) {
        selectedYear = year
    }

    // ================= DATA =================
    val recentTransactions: LiveData<List<Transaction>> =
        transactionRepo.getRecent()

    val allCategories: LiveData<List<Category>> =
        categoryRepo.getAll()

    // ================= QUERY =================
    //Home sẽ gọi tới đây để xem ds gd
    fun getTransactions(): LiveData<List<Transaction>> {
        return when (filterMode) {
            FilterMode.DAY -> transactionRepo.getByDate(selectedDate)

            FilterMode.MONTH ->
                transactionRepo.getByMonth(selectedMonth, selectedYear.toString())

            FilterMode.YEAR ->
                transactionRepo.getByYear(selectedYear.toString())

            FilterMode.ALL ->
                transactionRepo.getAll()
        }
    }

    fun insertCategory(category: Category, onComplete: (Long) -> Unit) {
        categoryRepo.insert(category, onComplete)
    }

    fun getIncome(): LiveData<Double> =
        transactionRepo.getTotalByTypeAndMonth(
            "income",
            selectedMonth,
            selectedYear.toString()
        )

    fun getExpense(): LiveData<Double> =
        transactionRepo.getTotalByTypeAndMonth(
            "expense",
            selectedMonth,
            selectedYear.toString()
        )

    fun getCategorySpending(): LiveData<List<CategoryTotal>> {
        return when (filterMode) {
            FilterMode.DAY ->
                transactionRepo.getSpendingByDate(selectedDate)

            FilterMode.MONTH ->
                transactionRepo.getSpendingByCategory(
                    selectedMonth,
                    selectedYear.toString()
                )

            FilterMode.YEAR ->
                transactionRepo.getSpendingByYear(
                    selectedYear.toString()
                )

            FilterMode.ALL ->
                transactionRepo.getAllSpendingByCategory()
        }
    }

    fun categorySpending(month: Int = selectedMonth, year: Int = selectedYear): LiveData<List<CategoryTotal>> {
        return transactionRepo.getSpendingByCategory(month, year.toString())
    }

    // ================= CRUD =================
    //Thêm gd
    fun insert(transaction: Transaction) = transactionRepo.insert(transaction)
    //lưu thay đổi
    fun update(transaction: Transaction) = transactionRepo.update(transaction)
    //xóa gd
    fun delete(transaction: Transaction) = transactionRepo.delete(transaction)

    fun search(keyword: String) = transactionRepo.search(keyword)

    //edit lấy dữ liệu cũ từ đây
    fun getById(id: Int): LiveData<Transaction?> =
        transactionRepo.getById(id)

    fun getTodayExpense(startOfDay: Long, endOfDay: Long): LiveData<Double> =
        transactionRepo.getTodayExpense(startOfDay, endOfDay)
}