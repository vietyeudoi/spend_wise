package com.example.spendwise.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.data.entity.Transaction
import com.example.spendwise.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Hệ màu sắc Modern UI cao cấp
val BrandBlue = Color(0xFF007AFF)
val ExpenseRed = Color(0xFFFF3B30)
val IncomeGreen = Color(0xFF34C759)
val SurfaceWhite = Color(0xFFFFFFFF)
val BackgroundGray = Color(0xFFF2F2F7)
val ContentGray = Color(0xFF8E8E93)
val TextDark = Color(0xFF1C1C1E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    vm: TransactionViewModel = viewModel()
) {
    val totalIncome by vm.getIncome().observeAsState(0.0)
    val totalExpense by vm.getExpense().observeAsState(0.0)
    val transactions by vm.getTransactions().observeAsState(initial = emptyList())

    val budgetAmount = vm.budget
    val remainingBudget = budgetAmount - totalExpense

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            Column(modifier = Modifier.background(SurfaceWhite)) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { vm.prevMonth() }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Tháng trước", tint = TextDark)
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BackgroundGray,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%d-%02d", vm.selectedYear, vm.selectedMonth),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
                                )
                            }
                            IconButton(onClick = { vm.nextMonth() }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Tháng sau", tint = TextDark)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Hành động mở lịch */ }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Lịch", tint = BrandBlue)
                        }
                    }
                )
                FilterTimeTabs(vm)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = BrandBlue,
                contentColor = SurfaceWhite,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Thêm ghi chép", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->

        // Nhóm các giao dịch dựa theo thuộc tính ngày tháng dạng số Long từ DB thật
        val groupedTransactions = transactions.groupBy { transaction ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                sdf.format(Date(transaction.date))
            } catch (e: Exception) {
                "Hạn mốc khác"
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                DashboardCard(
                    balance = totalIncome - totalExpense,
                    income = totalIncome,
                    expense = totalExpense,
                    budget = budgetAmount,
                    remaining = remainingBudget
                )
            }

            item {
                Text(
                    text = "Danh sách giao dịch",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateComponent("Không có dữ liệu chi tiêu trong thời gian này")
                }
            } else {
                groupedTransactions.forEach { (dateHeader, list) ->
                    stickyHeader {
                        DateHeaderRow(dateHeader)
                    }
                    items(list) { transaction ->
                        ModernTransactionRowItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    balance: Double,
    income: Double,
    expense: Double,
    budget: Double,
    remaining: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Tổng số dư", color = ContentGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                text = formatMoney(balance),
                color = if (balance >= 0) TextDark else ExpenseRed,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundGray, thickness = 1.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Thu nhập", color = ContentGray, fontSize = 12.sp)
                    Text(text = formatMoney(income), color = IncomeGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(35.dp).background(BackgroundGray))
                Column(modifier = Modifier.weight(1f).padding(start = 20.dp)) {
                    Text(text = "Chi tiêu", color = ContentGray, fontSize = 12.sp)
                    Text(text = formatMoney(expense), color = ExpenseRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundGray, thickness = 1.dp)

            val progress = if (budget > 0) (expense / budget).toFloat().coerceIn(0f, 1f) else 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row {
                        Text(text = "Hạn mức ngân sách: ", color = ContentGray, fontSize = 12.sp)
                        Text(text = formatMoney(budget), color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "Còn lại: ${formatMoney(remaining)}",
                        color = if (remaining >= 0) BrandBlue else ExpenseRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (progress > 0.8f) ExpenseRed else BrandBlue
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (progress > 0.8f) ExpenseRed else BrandBlue,
                trackColor = BackgroundGray,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun FilterTimeTabs(vm: TransactionViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val modes = listOf(
            TransactionViewModel.FilterMode.DAY to "Ngày",
            TransactionViewModel.FilterMode.MONTH to "Tháng",
            TransactionViewModel.FilterMode.YEAR to "Năm",
            TransactionViewModel.FilterMode.ALL to "Tất cả"
        )
        modes.forEach { (mode, label) ->
            val isSelected = vm.filterMode == mode
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) BrandBlue else BackgroundGray,
                modifier = Modifier
                    .weight(1f)
                    .clickable { vm.changeFilterMode(mode) }
            ) {
                Text(
                    text = label,
                    color = if (isSelected) SurfaceWhite else ContentGray,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DateHeaderRow(dateString: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = dateString, color = ContentGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModernTransactionRowItem(transaction: Transaction) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = BackgroundGray
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = transaction.title.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            fontSize = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = transaction.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                    if (transaction.note.isNotBlank()) {
                        Text(text = transaction.note, color = ContentGray, fontSize = 12.sp)
                    }
                }
            }

            val isIncome = transaction.type.equals("income", ignoreCase = true)
            Text(
                text = if (isIncome) "+${formatMoney(transaction.amount)}" else "-${formatMoney(transaction.amount)}",
                color = if (isIncome) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun EmptyStateComponent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = ContentGray, fontSize = 14.sp)
    }
}

fun formatMoney(amount: Double): String {
    return String.format(Locale.getDefault(), "%,.0f đ", amount)
}