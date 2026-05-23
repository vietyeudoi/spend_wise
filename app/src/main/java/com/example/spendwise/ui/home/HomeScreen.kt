package com.example.spendwise.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.data.entity.Transaction
import com.example.spendwise.viewmodel.TransactionViewModel

@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    vm: TransactionViewModel = viewModel()
) {
    val totalIncome  by vm.totalIncome.observeAsState(0.0)
    val totalExpense by vm.totalExpense.observeAsState(0.0)
    val recentList   by vm.recentTransactions.observeAsState(emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Thẻ tổng quan ──────────────────────────────────────────────────
            item {
                SummaryCard(totalIncome = totalIncome, totalExpense = totalExpense)
            }

            item {
                Text("Giao dịch gần đây", style = MaterialTheme.typography.titleMedium)
            }

            // ── Danh sách giao dịch ────────────────────────────────────────────
            items(recentList) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
fun SummaryCard(totalIncome: Double, totalExpense: Double) {
    val balance = totalIncome - totalExpense
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tháng này", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text  = formatMoney(balance),
                style = MaterialTheme.typography.headlineMedium,
                color = if (balance >= 0) Color(0xFF1A5C3A) else Color(0xFFB00020)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Thu nhập", style = MaterialTheme.typography.labelSmall)
                    Text(
                        formatMoney(totalIncome),
                        color = Color(0xFF1A5C3A),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Chi tiêu", style = MaterialTheme.typography.labelSmall)
                    Text(
                        formatMoney(totalExpense),
                        color = Color(0xFFB00020),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(transaction.title, style = MaterialTheme.typography.bodyLarge)
                Text(transaction.note,  style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text  = (if (transaction.type == "income") "+" else "-") +
                        formatMoney(transaction.amount),
                color = if (transaction.type == "income") Color(0xFF1A5C3A) else Color(0xFFB00020),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun formatMoney(amount: Double): String = "%,.0f đ".format(amount)
