package com.example.spendwise.data.repository
// Khai báo package chứa lớp Repository cho ngân sách

import android.app.Application // Import Application để lấy context
import androidx.lifecycle.LiveData // Import LiveData để quan sát dữ liệu
import com.example.spendwise.data.dao.BudgetDao // Import DAO của Budget
import com.example.spendwise.data.database.AppDatabase // Import AppDatabase (Room)
import com.example.spendwise.data.entity.Budget // Import entity Budget

class BudgetRepository(application: Application){
    // Lớp Repository, trung gian giữa ViewModel và DAO

    private val dao:BudgetDao =
        AppDatabase
            .getInstance(application)
            .budgetDao()
    // Lấy đối tượng DAO từ AppDatabase để thao tác với bảng Budget

    fun getByMonth(month:Int, year:Int):LiveData<List<Budget>>{
        return dao.getByMonth(month, year)
    }
    // Hàm lấy danh sách ngân sách theo tháng/năm
    // Trả về LiveData để UI quan sát và tự động cập nhật

    suspend fun insert(budget:Budget){
        dao.insert(budget)
    }
    // Hàm thêm ngân sách mới vào DB (chạy trong coroutine)

    suspend fun update(budget:Budget){
        dao.update(budget)
    }
    // Hàm cập nhật ngân sách trong DB

    suspend fun delete(budget:Budget){
        dao.delete(budget)
    }
    // Hàm xóa ngân sách trong DB

    suspend fun checkExist(categoryId:Int, month:Int, year:Int):Budget?{
        return dao.getByCategoryAndMonth(categoryId, month, year)
    }
    // Hàm kiểm tra xem ngân sách cho category/tháng/năm đã tồn tại chưa
    // Nếu có thì trả về Budget, nếu không thì null

    fun getTotalBudgetLimit(month: Int, year: Int): LiveData<Double> {
        return dao.getTotalBudgetLimit(month, year)
    }
    // Hàm lấy tổng hạn mức ngân sách của tháng/năm
    // Trả về LiveData để UI hiển thị bằng Text hoặc ProgressBar
}
