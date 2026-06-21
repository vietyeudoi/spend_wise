package com.example.spendwise.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.ui.home.*
import com.example.spendwise.viewmodel.TransactionViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Locale // ĐÃ THÊM: Import thư viện Locale để định dạng chuỗi phần trăm (%) tỷ lệ chi tiêu

val ChartColors = listOf(
    Color(0xFF007AFF).hashCode(), Color(0xFF34C759).hashCode(), Color(0xFFFF9500).hashCode(),
    Color(0xFFFF2D55).hashCode(), Color(0xAF5856D6).hashCode(), Color(0xFF4CD964).hashCode()
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: TransactionViewModel = viewModel()) {

    val spending by vm.getCategorySpending().observeAsState(emptyList())
    val totalSpending = spending.sumOf { it.total }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SurfaceWhite
                ),
                title = {
                    Text(
                        "Thống kê chi tiêu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            )
        }
    ) { padding ->

        if (spending.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có dữ liệu tháng này", color = ContentGray)
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    Card(
                        shape = RoundedCornerShape(24.dp)
                    ) {

                        Column(Modifier.padding(16.dp)) {

                            Text(
                                "Cơ cấu chi tiêu",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(12.dp))

                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                factory = { context ->
                                    PieChart(context).apply {
                                        description.isEnabled = false
                                        isDrawHoleEnabled = true
                                        holeRadius = 65f
                                        setHoleColor(android.graphics.Color.TRANSPARENT)
                                        legend.isEnabled = false
                                        setDrawEntryLabels(false)
                                        animateY(800, Easing.EaseInOutQuad)
                                    }
                                },
                                update = { chart ->

                                    val entries = spending.map {
                                        PieEntry(it.total.toFloat(), it.categoryName)
                                    }

                                    val dataSet = PieDataSet(entries, "").apply {
                                        colors = ChartColors
                                        sliceSpace = 3f
                                        setDrawValues(false)
                                    }

                                    chart.data = PieData(dataSet)

                                    chart.centerText =
                                        "Tổng chi\n${formatMoney(totalSpending)}"

                                    chart.setCenterTextSize(14f)

                                    chart.invalidate()
                                }
                            )
                        }
                    }
                }

                itemsIndexed(spending) { idx, item ->

                    val color = Color(ChartColors[idx % ChartColors.size])

                    val pct =
                        if (totalSpending > 0)
                            (item.total / totalSpending) * 100
                        else 0.0

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(color, CircleShape)
                                )

                                Spacer(Modifier.width(12.dp))

                                Column {

                                    Text(
                                        item.categoryName,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        String.format(
                                            Locale.getDefault(),
                                            "Chiếm %.1f%%",
                                            pct
                                        ),
                                        color = ContentGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Text(
                                formatMoney(item.total),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}