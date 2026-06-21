package com.example.spendwise.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.spendwise.data.database.AppDatabase
import com.example.spendwise.utils.isOverBudget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetAlertService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkBudgetAndNotify()
        return START_NOT_STICKY
    }

    private fun checkBudgetAndNotify() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(applicationContext)
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR).toString()

            val budgets = db.budgetDao().getByMonthSync(month, calendar.get(Calendar.YEAR))
            val spendings = db.transactionDao().getSpendingByCategorySync(month, year)

            var overBudgetCount = 0
            val overBudgetCategories = mutableListOf<String>()

            budgets.forEach { budget ->
                val spent = spendings.find { it.categoryId == budget.categoryId }?.total ?: 0.0
                if (isOverBudget(spent, budget.limitAmount)) {
                    overBudgetCount++
                    val catName = spendings.find { it.categoryId == budget.categoryId }?.categoryName
                        ?: db.categoryDao().getById(budget.categoryId)?.name
                        ?: "Danh mục ${budget.categoryId}"
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
            stopSelf()
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
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notification)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BudgetAlertService::class.java)
            context.startService(intent)
            Toast.makeText(context, "Bắt đầu kiểm tra ngân sách...", Toast.LENGTH_SHORT).show()
        }
    }
}
