package com.example.spendwise.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.data.entity.Budget
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.TransactionViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    budgetVm: BudgetViewModel      = viewModel(),
    txVm: TransactionViewModel     = viewModel()
) {
    val budgets    by budgetVm.budgets.observeAsState(emptyList())
    val categories by txVm.allCategories.observeAsState(emptyList())
    val spending   by txVm.categorySpending.observeAsState(emptyList())

    var showDialog     by remember { mutableStateOf(false) }
    var selectedCatId  by remember { mutableStateOf<Int?>(null) }
    var limitInput     by remember { mutableStateOf("") }
    var catExpanded    by remember { mutableStateOf(false) }

    val now   = Calendar.getInstance()
    val month = now.get(Calendar.MONTH) + 1
    val year  = now.get(Calendar.YEAR)

    // ── Dialog thêm ngân sách ─────────────────────────────────────────────────
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Đặt ngân sách") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded         = catExpanded,
                        onExpandedChange = { catExpanded = it }
                    ) {
                        OutlinedTextField(
                            value         = categories.find { it.id == selectedCatId }?.name ?: "Chọn danh mục",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Danh mục") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded         = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            categories.filter { it.type == "expense" }.forEach { cat ->
                                DropdownMenuItem(
                                    text    = { Text(cat.name) },
                                    onClick = { selectedCatId = cat.id; catExpanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value           = limitInput,
                        onValueChange   = { limitInput = it },
                        label           = { Text("Hạn mức (đ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier        = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val limit = limitInput.toDoubleOrNull()
                    if (selectedCatId != null && limit != null && limit > 0) {
                        budgetVm.insert(Budget(
                            categoryId  = selectedCatId!!,
                            limitAmount = limit,
                            month       = month,
                            year        = year
                        ))
                        showDialog    = false
                        limitInput    = ""
                        selectedCatId = null
                    }
                }) { Text("Lưu") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm ngân sách")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Ngân sách tháng $month/$year", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(budgets) { budget ->
                    val catName = categories.find { it.id == budget.categoryId }?.name ?: "?"
                    val spent   = spending.find { it.categoryId == budget.categoryId }?.total ?: 0.0
                    val percent = (spent / budget.limitAmount).coerceIn(0.0, 1.0).toFloat()
                    BudgetItem(
                        categoryName = catName,
                        spent        = spent,
                        limit        = budget.limitAmount,
                        percent      = percent
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetItem(categoryName: String, spent: Double, limit: Double, percent: Float) {
    val isOver    = percent >= 0.8f
    val barColor  = if (isOver) Color(0xFFB00020) else Color(0xFF1A5C3A)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(categoryName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "%,.0f / %,.0f đ".format(spent, limit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier.fillMaxWidth(),
                color    = barColor,
            )
            if (isOver) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "⚠ Sắp vượt ngân sách!",
                    color = Color(0xFFB00020),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
