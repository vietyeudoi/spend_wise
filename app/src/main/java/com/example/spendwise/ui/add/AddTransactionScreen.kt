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
import com.example.spendwise.data.entity.Category
import com.example.spendwise.data.entity.Transaction
import com.example.spendwise.viewmodel.TransactionViewModel
import com.example.spendwise.utils.ThousandsSeparatorTransformation

@OptIn(ExperimentalMaterial3Api::class)
//thêm mới
@Composable
//mở thêm
fun AddTransactionScreen(
    transactionId: Int = -1,
    onNavigateBack: () -> Unit,
    vm: TransactionViewModel = viewModel()
) {
    //chuyển sang chế độ sửa
    val isEditMode = transactionId != -1
    val categories by vm.allCategories.observeAsState(emptyList())

    // ── FIX: Lấy transaction cũ theo id khi ở edit mode ─────────────────────────
    // getById trả về LiveData<Transaction?> — observeAsState với initial = null vì
    // dữ liệu chỉ có sau khi Room trả về (bất đồng bộ), tránh giả định có sẵn ngay.
    val existingTransaction by if (isEditMode) {
        vm.getById(transactionId).observeAsState(initial = null)
    } else {
        remember { mutableStateOf<Transaction?>(null) }
    }

    // ── State ─────────────────────────────────────────────────────────────────
    //edit: đỗ dữ liệu cũ lên đây
    var title       by remember { mutableStateOf("") }
    var amount      by remember { mutableStateOf("") }
    var note        by remember { mutableStateOf("") }
    var type        by remember { mutableStateOf("expense") }
    var categoryId  by remember { mutableStateOf<Int?>(null) }
    var typeExpanded     by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // ── State cho tạo danh mục mới ngay trong dropdown (giống BudgetScreen) ────
    var isCreatingCategory by remember { mutableStateOf(false) }
    var newCategoryName    by remember { mutableStateOf("") }

    // ── FIX: Cờ đảm bảo chỉ fill dữ liệu vào form MỘT LẦN duy nhất khi
    // existingTransaction lần đầu có giá trị (LiveData có thể emit lại nhiều lần
    // khi dữ liệu trong DB thay đổi — nếu không có cờ này, mọi lần emit lại sẽ
    // ghi đè đè lên những gì người dùng đang gõ, làm mất nội dung đang sửa).
    var hasPrefilled by remember { mutableStateOf(false) }

    LaunchedEffect(existingTransaction) {
        val tx = existingTransaction
        if (tx != null && !hasPrefilled) {
            title      = tx.title
            amount     = tx.amount.toLong().toString()
            note       = tx.note
            type       = tx.type
            categoryId = tx.categoryId
            hasPrefilled = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Sửa giao dịch" else "Thêm giao dịch") },
                navigationIcon = {
                    //nút thêm, sửa
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
                //it đại diện cho chuỗi
                label         = { Text("Tên giao dịch") },
                modifier      = Modifier.fillMaxWidth(),
                //dl nằm trên 1 dòng duy nhất, enter là done
                singleLine    = true
            )

            // ── Số tiền ───────────────────────────────────────────────────────
            OutlinedTextField(
                value         = amount,
                onValueChange = { amount = it },
                label         = { Text("Số tiền (đ)") },
                suffix = { Text("đ") },
                //tự động thêm ,.
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
                        onClick = {
                            type = "expense"
                            typeExpanded = false
                            // FIX: đổi loại thì danh mục cũ (thuộc loại khác) không còn hợp lệ
                            categoryId = null
                        }
                    )
                    DropdownMenuItem(
                        text    = { Text("Thu nhập") },
                        onClick = {
                            type = "income"
                            typeExpanded = false
                            categoryId = null
                        }
                    )
                }
            }

            // ── Danh mục (kèm tạo danh mục mới — giống pattern BudgetScreen) ────
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

                    // FIX: Thêm tùy chọn "+ Thêm danh mục mới" — cùng pattern như BudgetScreen
                    DropdownMenuItem(
                        text    = { Text("+ Thêm danh mục mới") },
                        onClick = {
                            isCreatingCategory = true
                            categoryExpanded = false
                        }
                    )
                }
            }

            // ── Form tạo danh mục mới (hiện ngay dưới dropdown khi bật) ─────────
            if (isCreatingCategory) {
                OutlinedTextField(
                    value         = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label         = { Text("Tên danh mục mới") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                // type hiện tại (income/expense) quyết định loại danh mục mới,
                                // đúng với việc filteredCategories đang lọc theo `type`.
                                vm.insertCategory(
                                    Category(name = newCategoryName.trim(), icon = "ic_other", type = type)
                                ) { newId ->
                                    categoryId = newId.toInt()
                                }
                                isCreatingCategory = false
                                newCategoryName = ""
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tạo danh mục")
                    }

                    OutlinedButton(
                        onClick = {
                            isCreatingCategory = false
                            newCategoryName = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hủy")
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
                                // FIX: giữ nguyên ngày gốc khi sửa giao dịch, không ghi đè
                                // thành thời điểm hiện tại — nếu không, mỗi lần sửa sẽ làm
                                // giao dịch "nhảy" lên đầu danh sách theo ngày mới.
                                date       = existingTransaction?.date ?: System.currentTimeMillis()
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