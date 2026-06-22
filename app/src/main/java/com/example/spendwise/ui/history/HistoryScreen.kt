package com.example.spendwise.ui.history

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendwise.data.entity.Transaction
import com.example.spendwise.ui.home.formatMoney
import com.example.spendwise.utils.ExportUtils
import com.example.spendwise.utils.OverTextColor
import com.example.spendwise.utils.isOverBudget
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Search
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    vm: TransactionViewModel = viewModel(),
    budgetVm: BudgetViewModel = viewModel(),
    categoryId: Int = -1
) {

    val context = LocalContext.current

    var mode by remember {
        mutableStateOf(TransactionViewModel.FilterMode.MONTH)
    }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(mode) {

        vm.changeFilterMode(mode)

        when (mode) {
            TransactionViewModel.FilterMode.MONTH ->
                vm.setMonthYear(
                    vm.selectedMonth,
                    vm.selectedYear
                )

            TransactionViewModel.FilterMode.YEAR ->
                vm.setYear(vm.selectedYear)

            else -> {}
        }
    }

    val transactions by vm.getTransactions()
        .observeAsState(emptyList())

    val income by vm.getIncome()
        .observeAsState(0.0)

    val expense by vm.getExpense()
        .observeAsState(0.0)

    val categories by vm.allCategories.observeAsState(emptyList())
    val budgets by budgetVm.getBudgetsForMonth(vm.selectedMonth, vm.selectedYear).observeAsState(emptyList())
    val spending by vm.categorySpending().observeAsState(emptyList())

    var activeCategoryId by remember(categoryId) {
        mutableStateOf(categoryId)
    }
    var showExportMenu by remember { mutableStateOf(false) }

    val activeCategory = categories.find { it.id == activeCategoryId }

    val filteredTransactions = remember(transactions, activeCategoryId, searchQuery, categories) {
        val byCategory = if (activeCategoryId == -1) transactions
        else transactions.filter { it.categoryId == activeCategoryId }

        if (searchQuery.isBlank()) {
            byCategory
        } else {
            val q = searchQuery.trim().lowercase()
            byCategory.filter { t ->
                val catName = categories.find { it.id == t.categoryId }?.name ?: ""
                t.title.lowercase().contains(q) ||
                        t.note.lowercase().contains(q) ||
                        catName.lowercase().contains(q)
            }
        }
    }

    val totalIncome = remember(filteredTransactions, income) {
        if (activeCategoryId == -1) (income ?: 0.0)
        else filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredTransactions, expense) {
        if (activeCategoryId == -1) (expense ?: 0.0)
        else filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    }

    val grouped = filteredTransactions.groupBy {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Date(it.date))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch sử giao dịch",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { ExportUtils.exportToCsv(context, filteredTransactions) }) {


// Thêm state trong Composable
                        var showExportMenu by remember { mutableStateOf(false) }

// Thay icon Share cũ bằng:
                        Box {
                            IconButton(onClick = { showExportMenu = true }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Xuất dữ liệu")
                            }
                            DropdownMenu(
                                expanded = showExportMenu,
                                onDismissRequest = { showExportMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Chia sẻ") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                    onClick = {
                                        ExportUtils.shareCsv(context, filteredTransactions)
                                        showExportMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Lưu về máy") },
                                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                                    onClick = {
                                        ExportUtils.saveToDevice(context, filteredTransactions)
                                        showExportMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly
                ) {

                    SummaryItem(
                        "Thu",
                        formatMoney(totalIncome),
                        Color(0xFF4CAF50)
                    )

                    SummaryItem(
                        "Chi",
                        formatMoney(totalExpense),
                        Color.Red
                    )

                    SummaryItem(
                        "GD",
                        filteredTransactions.size.toString(),
                        Color.Blue
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm theo tên, ghi chú, danh mục...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(
                    selected =
                        mode ==
                                TransactionViewModel.FilterMode.DAY,
                    onClick = {

                        mode =
                            TransactionViewModel.FilterMode.DAY

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->

                                vm.setDate(
                                    "%04d-%02d-%02d".format(
                                        y,
                                        m + 1,
                                        d
                                    )
                                )
                            },
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH),
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    label = { Text("Ngày") }
                )

                FilterChip(
                    selected =
                        mode ==
                                TransactionViewModel.FilterMode.MONTH,
                    onClick = {
                        mode =
                            TransactionViewModel.FilterMode.MONTH
                    },
                    label = { Text("Tháng") }
                )

                FilterChip(
                    selected =
                        mode ==
                                TransactionViewModel.FilterMode.YEAR,
                    onClick = {
                        mode =
                            TransactionViewModel.FilterMode.YEAR
                    },
                    label = { Text("Năm") }
                )

                FilterChip(
                    selected =
                        mode ==
                                TransactionViewModel.FilterMode.ALL,
                    onClick = {
                        mode =
                            TransactionViewModel.FilterMode.ALL
                    },
                    label = { Text("Tất cả") }
                )
            }

            Spacer(Modifier.height(8.dp))

            if (mode == TransactionViewModel.FilterMode.MONTH) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {
                                vm.prevMonth()
                            }
                        ) {
                            Text("◀")
                        }

                        Text(
                            text =
                                "Tháng ${vm.selectedMonth}/${vm.selectedYear}",
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                vm.nextMonth()
                            }
                        ) {
                            Text("▶")
                        }
                    }
                }
            }

            if (activeCategory != null) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đang lọc: ${activeCategory.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { activeCategoryId = -1 },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Xóa lọc",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filteredTransactions.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        "📭 Không có giao dịch",
                        color = Color.Gray
                    )
                }
            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {

                    grouped.forEach { (date, list) ->

                        item {

                            Text(
                                text = date,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        items(list) { item ->
                            val itemBudget = budgets.find { it.categoryId == item.categoryId }
                            val itemSpent = spending.find { it.categoryId == item.categoryId }?.total ?: 0.0
                            val isOver = if (itemBudget != null) isOverBudget(itemSpent, itemBudget.limitAmount) else false
                            val catName = categories.find { it.id == item.categoryId }?.name ?: ""
//Xem chi tiết
                            TransactionItem(
                                t = item,
                                isCategoryOverBudget = isOver,
                                categoryName = catName,
                                onClick = {
                                    navController.navigate(
                                        "transaction_detail/${item.id}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    title: String,
    value: String,
    color: Color
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            title,
            color = Color.Gray
        )

        Text(
            value,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionItem(
    t: Transaction,
    isCategoryOverBudget: Boolean,
    categoryName: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 4.dp
            )
            .clickable {
                onClick()
            }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    if (t.type == "income")
                        "🟢"
                    else
                        "🔴"
                )

                Spacer(
                    Modifier.width(10.dp)
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            t.title,
                            fontWeight = FontWeight.Bold
                        )
                        if (categoryName.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = categoryName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isCategoryOverBudget) {
                                        Spacer(Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(OverTextColor, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (t.note.isNotBlank()) {

                        Text(
                            t.note,
                            color = Color.Gray,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(
                text =
                    if (t.type == "income")
                        "+${formatMoney(t.amount)}"
                    else
                        "-${formatMoney(t.amount)}",
                color =
                    if (t.type == "income")
                        Color(0xFF4CAF50)
                    else
                        Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}