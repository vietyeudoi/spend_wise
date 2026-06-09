package com.example.spendwise.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ✅ IMPORT QUAN TRỌNG NHẤT (BẮT BUỘC)
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState

import com.example.spendwise.viewmodel.TransactionViewModel
import com.example.spendwise.ui.home.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    vm: TransactionViewModel = viewModel()
) {

    // ===== DATA REAL =====
    val budgetAmount = vm.budget
    val expense by vm.getExpense().observeAsState(0.0)

    val spentAmount = expense ?: 0.0
    val remaining = budgetAmount - spentAmount

    val progress =
        if (budgetAmount <= 0) 0f
        else (spentAmount / budgetAmount).toFloat()

    val isOverBudget = spentAmount > budgetAmount

    // ===== STATE DIALOG =====
    var showDialog by remember { mutableStateOf(false) }
    var temp by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nhập ngân sách") },
            text = {
                OutlinedTextField(
                    value = temp,
                    onValueChange = { temp = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.setBudget(temp.toDoubleOrNull() ?: 0.0)
                    showDialog = false
                }) {
                    Text("Lưu")
                }
            }
        )
    }

    // ===== UI =====
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ngân sách") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ===== CARD =====
            Card {
                Column(Modifier.padding(16.dp)) {

                    Text("Ngân sách tháng")

                    Text(
                        formatMoney(budgetAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { progress.coerceAtMost(1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isOverBudget) Color.Red else Color.Blue
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Đã chi: ${formatMoney(spentAmount)}")
                    Text("Còn lại: ${formatMoney(remaining)}")

                    if (isOverBudget) {
                        Text(
                            "⚠ Vượt ngân sách",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ===== BUTTON =====
            Button(
                onClick = {
                    temp = budgetAmount.toInt().toString()
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Thiết lập ngân sách")
            }
        }
    }
}