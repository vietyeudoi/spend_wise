package com.example.spendwise.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.viewmodel.TransactionViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

@Composable
fun StatsScreen(vm: TransactionViewModel = viewModel()) {
    val spending by vm.categorySpending.observeAsState(emptyList())
    val primary  = MaterialTheme.colorScheme.primary.toArgb()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Thống kê chi tiêu", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (spending.isEmpty()) {
            Text("Chưa có dữ liệu chi tiêu tháng này",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            // ── MPAndroidChart PieChart nhúng vào Compose qua AndroidView ───────
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory  = { ctx -> PieChart(ctx) },
                update   = { chart ->
                    val entries = spending.map { PieEntry(it.total.toFloat(), it.categoryName as String) }
                    val dataSet = PieDataSet(entries, "Chi tiêu").apply {
                        colors = listOf(
                            android.graphics.Color.parseColor("#4CAF50"),
                            android.graphics.Color.parseColor("#2196F3"),
                            android.graphics.Color.parseColor("#FF9800"),
                            android.graphics.Color.parseColor("#F44336"),
                            android.graphics.Color.parseColor("#9C27B0"),
                            android.graphics.Color.parseColor("#00BCD4"),
                        )
                        valueTextSize = 12f
                    }
                    chart.data        = PieData(dataSet)
                    chart.description.isEnabled = false
                    chart.isDrawHoleEnabled     = true
                    chart.holeRadius            = 50f
                    chart.setEntryLabelColor(android.graphics.Color.BLACK)
                    chart.invalidate()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Danh sách chú thích ───────────────────────────────────────────
            spending.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.categoryName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "%,.0f đ".format(item.total),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider()
            }
        }
    }
}
