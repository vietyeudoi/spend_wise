package com.example.spendwise.viewmodel // Khai báo package chứa ViewModel

import android.app.Application // Import Application để dùng context
import androidx.compose.runtime.* // Import để dùng mutableStateOf (Compose)
import androidx.lifecycle.* // Import LiveData, ViewModelScope...
import com.example.spendwise.data.entity.Budget // Import entity Budget
import com.example.spendwise.data.repository.BudgetRepository // Import repository Budget
import com.example.spendwise.data.repository.CategoryRepository // Import repository Category
import kotlinx.coroutines.launch // Import coroutine để chạy bất đồng bộ
import java.util.Calendar // Import Calendar để lấy tháng/năm hiện tại

class BudgetViewModel(application:Application):AndroidViewModel(application) {
    // Tạo ViewModel quản lý dữ liệu ngân sách, kế thừa AndroidViewModel để có context

    private val repo = BudgetRepository(application)
    // Repository để thao tác với bảng Budget (thêm, sửa, xóa, lấy dữ liệu)

    private val categoryRepo = CategoryRepository.getInstance(application)
    // Repository để thao tác với bảng Category

    private val cal = Calendar.getInstance()
    // Lấy thời gian hiện tại từ hệ thống

    var selectedMonth by mutableStateOf(cal.get(Calendar.MONTH)+1)
        private set
    // Biến trạng thái lưu tháng hiện tại (Compose sẽ tự động cập nhật UI khi thay đổi)

    var selectedYear by mutableStateOf(cal.get(Calendar.YEAR))
        private set
    // Biến trạng thái lưu năm hiện tại

    val budgets:LiveData<List<Budget>>
        get() = repo.getByMonth(selectedMonth, selectedYear)
    // LiveData danh sách ngân sách theo tháng/năm hiện tại
    // UI (BudgetScreen.kt) sẽ observe và hiển thị bằng LazyColumn

    fun getBudgetsForMonth(month: Int, year: Int): LiveData<List<Budget>> {
        return repo.getByMonth(month, year)
    }
    // Hàm lấy ngân sách theo tháng/năm bất kỳ

    val totalBudgetLimit: LiveData<Double>
        get() = repo.getTotalBudgetLimit(selectedMonth, selectedYear)
    // LiveData tổng hạn mức ngân sách của tháng/năm hiện tại
    // UI sẽ observe và hiển thị bằng Text() hoặc ProgressBar

    fun getTotalBudgetLimit(month: Int, year: Int): LiveData<Double> {
        return repo.getTotalBudgetLimit(month, year)
    }
    // Hàm lấy tổng hạn mức ngân sách theo tháng/năm bất kỳ

    fun nextMonth(){
        if(selectedMonth==12){
            selectedMonth=1
            selectedYear++
        } else {
            selectedMonth++
        }
    }
    // Chuyển sang tháng kế tiếp, nếu tháng 12 thì reset về tháng 1 và tăng năm

    fun prevMonth(){
        if(selectedMonth==1){
            selectedMonth=12
            selectedYear--
        } else {
            selectedMonth--
        }
    }
    // Chuyển về tháng trước, nếu tháng 1 thì quay về tháng 12 và giảm năm

    fun insert(budget:Budget){
        viewModelScope.launch {
            val old = repo.checkExist(budget.categoryId, budget.month, budget.year)
            // Kiểm tra xem ngân sách cho category/tháng/năm này đã tồn tại chưa

            if(old!=null){
                repo.update(budget.copy(id = old.id))
                // Nếu tồn tại thì update lại với id cũ
            } else {
                repo.insert(budget)
                // Nếu chưa có thì insert mới
            }
        }
    }
    // Hàm thêm ngân sách, nhận dữ liệu từ UI (người dùng nhập form)

    fun update(budget:Budget){
        viewModelScope.launch {
            repo.update(budget)
        }
    }
    // Hàm cập nhật ngân sách, nhận dữ liệu từ UI

    fun delete(budget:Budget){
        viewModelScope.launch {
            repo.delete(budget)
        }
    }
    // Hàm xóa ngân sách, nhận dữ liệu từ UI

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
        // Tạo category mới với tên, icon mặc định, type = expense

        categoryRepo.insert(newCategory) { newId ->
            // Insert category, lấy id mới qua callback

            val budget = Budget(
                categoryId  = newId.toInt(),
                limitAmount = limitAmount,
                month       = month,
                year        = year
            )
            // Tạo ngân sách mới gắn với category vừa tạo

            viewModelScope.launch {
                val old = repo.checkExist(budget.categoryId, month, year)
                if (old != null) {
                    repo.update(budget.copy(id = old.id))
                    // Nếu đã có ngân sách cho category này thì update
                } else {
                    repo.insert(budget)
                    // Nếu chưa có thì insert mới
                }
                onComplete()
                // Gọi callback báo hoàn thành (UI có thể đóng dialog hoặc refresh)
            }
        }
    }
    // Hàm tiện lợi: vừa tạo category mới vừa tạo ngân sách cho nó
}
