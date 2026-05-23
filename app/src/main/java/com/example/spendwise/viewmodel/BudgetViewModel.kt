package com.example.spendwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.spendwise.data.entity.Budget
import com.example.spendwise.data.repository.BudgetRepository
import java.util.Calendar

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BudgetRepository(application)

    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH) + 1
    private val currentYear = calendar.get(Calendar.YEAR)

    val budgets: LiveData<List<Budget>> = repo.getByMonth(currentMonth, currentYear)

    fun insert(budget: Budget) = repo.insert(budget)

    fun update(budget: Budget) = repo.update(budget)

    fun delete(budget: Budget) = repo.delete(budget)
}