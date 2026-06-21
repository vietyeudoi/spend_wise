package com.example.spendwise.viewmodel


import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.spendwise.data.entity.Budget
import com.example.spendwise.data.repository.BudgetRepository
import com.example.spendwise.data.repository.CategoryRepository
import kotlinx.coroutines.launch
import java.util.Calendar



class BudgetViewModel(
    application:Application
):AndroidViewModel(application){



    private val repo =
        BudgetRepository(application)

    private val categoryRepo = CategoryRepository.getInstance(application)



    private val cal =
        Calendar.getInstance()



    var selectedMonth by mutableStateOf(
        cal.get(Calendar.MONTH)+1
    )
        private set



    var selectedYear by mutableStateOf(
        cal.get(Calendar.YEAR)
    )
        private set




    val budgets:LiveData<List<Budget>>
        get() =
            repo.getByMonth(
                selectedMonth,
                selectedYear
            )

    fun getBudgetsForMonth(month: Int, year: Int): LiveData<List<Budget>> {
        return repo.getByMonth(month, year)
    }

    val totalBudgetLimit: LiveData<Double>
        get() = repo.getTotalBudgetLimit(selectedMonth, selectedYear)

    fun getTotalBudgetLimit(month: Int, year: Int): LiveData<Double> {
        return repo.getTotalBudgetLimit(month, year)
    }





    fun nextMonth(){

        if(selectedMonth==12){

            selectedMonth=1
            selectedYear++

        }
        else{

            selectedMonth++

        }

    }




    fun prevMonth(){

        if(selectedMonth==1){

            selectedMonth=12
            selectedYear--

        }
        else{

            selectedMonth--

        }

    }





    fun insert(
        budget:Budget
    ){

        viewModelScope.launch {


            val old =
                repo.checkExist(
                    budget.categoryId,
                    budget.month,
                    budget.year
                )


            if(old!=null){

                repo.update(
                    budget.copy(
                        id = old.id
                    )
                )

            }
            else{

                repo.insert(
                    budget
                )

            }


        }

    }




    fun update(
        budget:Budget
    ){

        viewModelScope.launch {

            repo.update(budget)

        }

    }




    fun delete(
        budget:Budget
    ){

        viewModelScope.launch {

            repo.delete(budget)

        }

    }

    fun insertCategoryAndBudget(
        categoryName: String,
        limitAmount: Double,
        month: Int,
        year: Int,
        onComplete: () -> Unit
    ) {
        val newCategory = com.example.spendwise.data.entity.Category(
            name = categoryName,
            icon = "ic_custom",
            type = "expense"
        )

        // Insert category trước, lấy id qua callback (chạy trên main thread, an toàn)
        categoryRepo.insert(newCategory) { newId ->
            val budget = Budget(
                categoryId  = newId.toInt(),   // ép kiểu Long → Int
                limitAmount = limitAmount,
                month       = month,
                year        = year
            )

            viewModelScope.launch {
                val old = repo.checkExist(budget.categoryId, month, year)
                if (old != null) {
                    repo.update(budget.copy(id = old.id))
                } else {
                    repo.insert(budget)
                }
                onComplete()
            }
        }
    }

}