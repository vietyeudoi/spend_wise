package com.example.spendwise.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.spendwise.data.entity.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(categories: List<Category>)

    @Update
    fun update(category: Category)

    @Delete
    fun delete(category: Category)

    // Lấy tất cả danh mục
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAll(): LiveData<List<Category>>

    // Lấy theo loại: "income" hoặc "expense"
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getByType(type: String): LiveData<List<Category>>

    // Lấy một danh mục theo id (dùng khi hiển thị chi tiết)
    @Query("SELECT * FROM categories WHERE id = :id")
    fun getById(id: Int): Category?
}
