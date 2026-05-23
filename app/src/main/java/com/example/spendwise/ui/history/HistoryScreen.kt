package com.example.spendwise.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.data.entity.Transaction
import com.example.spendwise.ui.home.formatMoney
import com.example.spendwise.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onNavigateToEdit: (Int) -> Unit,
    vm: TransactionViewModel = viewModel()
) {
    val transactions by vm.monthlyTransactions.observeAsState(emptyList())
    var deleteTarget by remember { mutableStateOf<Transaction?>(null) }

    // ── Dialog xác nhận xóa ───────────────────────────────────────────────────
    deleteTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title   = { Text("Xóa giao dịch?") },
            text    = { Text("Bạn có chắc muốn xóa \"${t.title}\" không?") },
            confirmButton = {
                TextButton(onClick = { vm.delete(t); deleteTarget = null }) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Hủy") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Lịch sử giao dịch", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có giao dịch nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transactions, key = { it.id }) { transaction ->
                    HistoryItem(
                        transaction  = transaction,
                        onClick      = { onNavigateToEdit(transaction.id) },
                        onLongClick  = { deleteTarget = transaction }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(
    transaction : Transaction,
    onClick     : () -> Unit,
    onLongClick : () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick     = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(transaction.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    dateFormat.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
