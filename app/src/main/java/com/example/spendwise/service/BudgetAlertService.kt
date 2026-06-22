package com.example.spendwise.service
// Khai báo package chứa Service cảnh báo ngân sách

import android.app.NotificationChannel // Import để tạo kênh thông báo (Android 8+)
import android.app.NotificationManager // Import để quản lý thông báo
import android.app.Service // Import Service (chạy ngầm)
import android.content.Context // Import Context
import android.content.Intent // Import Intent để khởi động Service
import android.os.Build // Import để kiểm tra phiên bản Android
import android.os.IBinder // Import IBinder (không dùng trong Service này)
import android.widget.Toast // Import Toast để hiển thị thông báo ngắn
import androidx.core.app.NotificationCompat // Import để tạo thông báo
import com.example.spendwise.data.database.AppDatabase // Import Room Database
import com.example.spendwise.utils.isOverBudget // Import hàm kiểm tra vượt ngân sách
import kotlinx.coroutines.* // Import coroutine để chạy bất đồng bộ
import java.util.Calendar // Import Calendar để lấy tháng/năm hiện tại

class BudgetAlertService : Service() {
    // Service chạy ngầm để kiểm tra ngân sách và phát thông báo

    override fun onBind(intent: Intent?): IBinder? = null
    // Service này không hỗ trợ bind, nên trả về null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkBudgetAndNotify() // Gọi hàm kiểm tra ngân sách và phát thông báo
        return START_NOT_STICKY // Service không tự khởi động lại nếu bị hệ thống kill
    }

    private fun checkBudgetAndNotify() {
        CoroutineScope(Dispatchers.IO).launch {
            // Chạy trên luồng IO để tránh chặn UI

            val db = AppDatabase.getInstance(applicationContext)
            // Lấy instance của Room Database

            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR).toString()
            // Lấy tháng/năm hiện tại

            val budgets = db.budgetDao().getByMonthSync(month, calendar.get(Calendar.YEAR))
            // Lấy danh sách ngân sách theo tháng/năm

            val spendings = db.transactionDao().getSpendingByCategorySync(month, year)
            // Lấy danh sách chi tiêu theo danh mục

            var overBudgetCount = 0
            val overBudgetCategories = mutableListOf<String>()
            // Biến đếm số danh mục vượt ngân sách + danh sách tên danh mục

            budgets.forEach { budget ->
                val spent = spendings.find { it.categoryId == budget.categoryId }?.total ?: 0.0
                // Tìm số tiền đã chi cho category này

                if (isOverBudget(spent, budget.limitAmount)) {
                    // Nếu vượt ngưỡng (>= 80% hạn mức)

                    overBudgetCount++
                    val catName = spendings.find { it.categoryId == budget.categoryId }?.categoryName
                        ?: db.categoryDao().getById(budget.categoryId)?.name
                        ?: "Danh mục ${budget.categoryId}"
                    // Lấy tên danh mục (nếu không có thì fallback)

                    overBudgetCategories.add(catName)
                }
            }

            if (overBudgetCount > 0) {
                showNotification(
                    "Cảnh báo ngân sách",
                    "Có $overBudgetCount danh mục đã vượt/chạm ngưỡng ngân sách: ${overBudgetCategories.joinToString()}"
                )
            } else {
                showNotification(
                    "Thông báo ngân sách",
                    "Tất cả danh mục đều nằm trong tầm kiểm soát."
                )
            }
            stopSelf() // Dừng Service sau khi chạy xong
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "budget_alert_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title) // Tiêu đề thông báo
            .setContentText(message) // Nội dung thông báo
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icon mặc định
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Độ ưu tiên cao
            .setAutoCancel(true) // Tự đóng khi người dùng bấm
            .build()

        notificationManager.notify(101, notification)
        // Hiển thị thông báo với ID = 101
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BudgetAlertService::class.java)
            context.startService(intent)
            Toast.makeText(context, "Bắt đầu kiểm tra ngân sách...", Toast.LENGTH_SHORT).show()
        }
    }
}
