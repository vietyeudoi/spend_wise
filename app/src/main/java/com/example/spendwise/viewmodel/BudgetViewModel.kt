package com.example.spendwise.viewmodel


import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.spendwise.data.entity.Budget
import com.example.spendwise.data.repository.BudgetRepository
import kotlinx.coroutines.launch
import java.util.Calendar



class BudgetViewModel(
    application:Application
):AndroidViewModel(application){



    private val repo =
        BudgetRepository(application)



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


}