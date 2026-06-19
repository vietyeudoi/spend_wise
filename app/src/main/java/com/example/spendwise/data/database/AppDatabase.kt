package com.example.spendwise.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spendwise.data.dao.BudgetDao
import com.example.spendwise.data.dao.CategoryDao
import com.example.spendwise.data.dao.TransactionDao
import com.example.spendwise.data.entity.Budget
import com.example.spendwise.data.entity.Category
import com.example.spendwise.data.entity.Transaction


@Database(
    entities = [
        Category::class,
        Transaction::class,
        Budget::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {


    abstract fun categoryDao(): CategoryDao

    abstract fun transactionDao(): TransactionDao

    abstract fun budgetDao(): BudgetDao



    companion object {


        @Volatile
        private var INSTANCE: AppDatabase? = null



        fun getInstance(
            context: Context
        ): AppDatabase {


            return INSTANCE ?: synchronized(this) {


                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "spendwise_db"
                    )
                        // Xóa database cũ khi đổi Entity
                        .fallbackToDestructiveMigration()
                        .build()



                INSTANCE = instance


                instance

            }


        }

    }

}