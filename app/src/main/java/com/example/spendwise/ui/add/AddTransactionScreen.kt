package com.example.spendwise.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.data.entity.Transaction
import com.example.spendwise.viewmodel.TransactionViewModel
import com.example.spendwise.utils.ThousandsSeparatorTransformation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Int = -1,
    onNavigateBack: () -> Unit,
    vm: TransactionViewModel = viewModel()
) {
    val isEditMode = transactionId != -1
    val categories by vm.allCategories.observeAsState(emptyList())

    // ── State ─────────────────────────────────────────────────────────────────
    var title       by remember { mutableStateOf("") }
    var amount      by remember { mutableStateOf("") }
    var note        by remember { mutableStateOf("") }
    var type        by remember { mutableStateOf("expense") }
    var categoryId  by remember { mutableStateOf<Int?>(null) }
    var typeExpanded     by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Sửa giao dịch" else "Thêm giao dịch") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Tên giao dịch ─────────────────────────────────────────────────
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                label         = { Text("Tên giao dịch") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            // ── Số tiền ───────────────────────────────────────────────────────
            OutlinedTextField(
                value         = amount,
                onValueChange = { amount = it },
                label         = { Text("Số tiền (đ)") },
                suffix = { Text("đ") },
                visualTransformation = ThousandsSeparatorTransformation(),
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine    = true
            )

            // ── Loại: thu / chi ───────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded        = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value         = if (type == "income") "Thu nhập" else "Chi tiêu",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Loại") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                    modifier      = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded        = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text("Chi tiêu") },
                        onClick = { type = "expense"; typeExpanded = false }
                    )
                    DropdownMenuItem(
                        text    = { Text("Thu nhập") },
                        onClick = { type = "income"; typeExpanded = false }
                    )
                }
            }

            // ── Danh mục ──────────────────────────────────────────────────────
            val filteredCategories = categories.filter { it.type == type }
            ExposedDropdownMenuBox(
                expanded         = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value         = filteredCategories.find { it.id == categoryId }?.name ?: "Chọn danh mục",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Danh mục") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier      = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded         = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    filteredCategories.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text(cat.name) },
                            onClick = { categoryId = cat.id; categoryExpanded = false }
                        )
                    }
                }
            }

            // ── Ghi chú ───────────────────────────────────────────────────────
            OutlinedTextField(
                value         = note,
                onValueChange = { note = it },
                label         = { Text("Ghi chú") },
                modifier      = Modifier.fillMaxWidth(),
                minLines      = 2
            )

            // ── Thông báo lỗi ────────────────────────────────────────────────
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            // ── Nút lưu ───────────────────────────────────────────────────────
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick  = {
                    val amountVal = amount.toDoubleOrNull()
                    when {
                        title.isBlank()        -> errorMessage = "Vui lòng nhập tên giao dịch"
                        amountVal == null
                        || amountVal <= 0      -> errorMessage = "Số tiền không hợp lệ"
                        else -> {
                            val transaction = Transaction(
                                id         = if (isEditMode) transactionId else 0,
                                title      = title.trim(),
                                amount     = amountVal,
                                type       = type,
                                categoryId = categoryId,
                                note       = note.trim(),
                                date       = System.currentTimeMillis()
                            )
                            if (isEditMode) vm.update(transaction)
                            else            vm.insert(transaction)
                            onNavigateBack()
                        }
                    }
                }
            ) {
                Text(if (isEditMode) "Lưu thay đổi" else "Thêm giao dịch")
            }
        }
    }
}
