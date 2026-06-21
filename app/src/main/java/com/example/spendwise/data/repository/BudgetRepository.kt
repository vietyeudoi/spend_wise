package com.example.spendwise.data.repository


import android.app.Application
import androidx.lifecycle.LiveData
import com.example.spendwise.data.dao.BudgetDao
import com.example.spendwise.data.database.AppDatabase
import com.example.spendwise.data.entity.Budget



class BudgetRepository(
    application: Application
){


    private val dao:BudgetDao =
        AppDatabase
            .getInstance(application)
            .budgetDao()



    fun getByMonth(
        month:Int,
        year:Int
    ):LiveData<List<Budget>>{

        return dao.getByMonth(
            month,
            year
        )

    }



    suspend fun insert(
        budget:Budget
    ){

        dao.insert(budget)

    }



    suspend fun update(
        budget:Budget
    ){

        dao.update(budget)

    }



    suspend fun delete(
        budget:Budget
    ){

        dao.delete(budget)

    }



    suspend fun checkExist(
        categoryId:Int,
        month:Int,
        year:Int
    ):Budget?{

        return dao.getByCategoryAndMonth(
            categoryId,
            month,
            year
        )

    }


}