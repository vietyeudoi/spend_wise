package com.example.spendwise.ui.history

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.spendwise.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    vm: TransactionViewModel = viewModel()
) {

    val context = LocalContext.current

    var mode by remember {
        mutableStateOf(TransactionViewModel.FilterMode.MONTH)
    }

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

    val totalIncome = income ?: 0.0
    val totalExpense = expense ?: 0.0

    val grouped = transactions.groupBy {
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
                        transactions.size.toString(),
                        Color.Blue
                    )
                }
            }

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

            Spacer(Modifier.height(8.dp))

            if (transactions.isEmpty()) {

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

                            TransactionItem(
                                t = item,
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
                    Alignment.CenterVertically
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

                    Text(
                        t.title,
                        fontWeight = FontWeight.Bold
                    )

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