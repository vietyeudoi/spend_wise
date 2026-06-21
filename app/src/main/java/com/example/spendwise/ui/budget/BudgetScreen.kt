package com.example.spendwise.ui.budget

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendwise.data.entity.Budget
import com.example.spendwise.navigation.Screen
import com.example.spendwise.ui.home.BrandBlue
import com.example.spendwise.ui.home.SurfaceWhite
import com.example.spendwise.ui.home.formatMoney
import com.example.spendwise.utils.*
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.TransactionViewModel
import androidx.compose.material.icons.filled.Delete
import com.example.spendwise.data.entity.Category
import com.example.spendwise.utils.ThousandsSeparatorTransformation

@Composable
fun BudgetScreen(
    navController: NavController,
    budgetVm: BudgetViewModel = viewModel(),
    txVm: TransactionViewModel = viewModel()
){



    val budgets by
    budgetVm.budgets.observeAsState(
        emptyList()
    )


    val categories by
    txVm.allCategories.observeAsState(
        emptyList()
    )

    val spending by txVm.categorySpending(budgetVm.selectedMonth, budgetVm.selectedYear).observeAsState(emptyList())



    var showDialog by remember{
        mutableStateOf(false)
    }


    var amount by remember{
        mutableStateOf("")
    }


    var selectedCategory by remember{
        mutableStateOf<Int?>(null)
    }

    // State cho thêm danh mục mới ngay trong dialog
    var isCreatingCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // State cho dialog sửa/xóa ngân sách
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var editAmount by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }



    Scaffold(

        floatingActionButton = {

            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = BrandBlue,
                contentColor = SurfaceWhite,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm ngân sách",
                    modifier = Modifier.size(24.dp)
                )

            }

        }

    ){padding->



        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ){


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ){

                Button(
                    onClick={
                        budgetVm.prevMonth()
                    }
                ){
                    Text("<")
                }



                Text(
                    "${budgetVm.selectedMonth}/${budgetVm.selectedYear}",
                    style =
                        MaterialTheme.typography.titleLarge
                )



                Button(
                    onClick={
                        budgetVm.nextMonth()
                    }
                ){
                    Text(">")
                }

            }





            LazyColumn{

                items(budgets) { budget ->
                    val name = categories.find { it.id == budget.categoryId }?.name ?: ""
                    val spent = spending.find { it.categoryId == budget.categoryId }?.total ?: 0.0
                    val isOver = isOverBudget(spent, budget.limitAmount)

                    val cardBgColor = if (isOver) OverContainerColor else NormalContainerColor
                    val textColor = if (isOver) OverTextColor else NormalTextColor

                    val progress = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat().coerceIn(0f, 1f) else 0f

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .clickable {
                                editingBudget = budget
                                editAmount = budget.limitAmount.toString()
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = if (isOver) "Vượt ngưỡng!" else "Bình thường",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Đã chi: ${formatMoney(spent)}",
                                    color = textColor.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Hạn mức: ${formatMoney(budget.limitAmount)}",
                                    color = textColor.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = textColor,
                                trackColor = textColor.copy(alpha = 0.2f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }
                }


            }


        }



// ── Dialog sửa / xóa ngân sách ─────────────────────────────────────────────
        editingBudget?.let { budget ->
            val categoryName = categories.find { it.id == budget.categoryId }?.name ?: ""

            AlertDialog(
                onDismissRequest = { editingBudget = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(categoryName)
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa ngân sách",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                text = {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("Hạn mức (đ)") },
                        suffix = { Text("đ") },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val newAmount = editAmount.toDoubleOrNull()
                        if (newAmount != null && newAmount > 0) {
                            budgetVm.update(budget.copy(limitAmount = newAmount))
                            editingBudget = null
                        }
                    }) {
                        Text("Lưu")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingBudget = null }) {
                        Text("Hủy")
                    }
                }
            )

            // ── Dialog xác nhận xóa (mở chồng lên trên) ────────────────────────────
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Xóa ngân sách?") },
                    text = { Text("Bạn có chắc muốn xóa ngân sách \"$categoryName\" không?") },
                    confirmButton = {
                        TextButton(onClick = {
                            budgetVm.delete(budget)
                            showDeleteConfirm = false
                            editingBudget = null
                        }) {
                            Text("Xóa", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }



    }

    // ── Dialog thêm ngân sách mới ──────────────────────────────────────────────
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Thêm ngân sách") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isCreatingCategory) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Tên danh mục mới") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(categories.find { it.id == selectedCategory }?.name ?: "Chọn danh mục")
                            }
                            DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                                categories.filter { it.type == "expense" }.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategory = category.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("+ Thêm danh mục mới") },
                                    onClick = {
                                        isCreatingCategory = true
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Số tiền") },
                        suffix = { Text("đ") },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val enteredAmount = amount.toDoubleOrNull() ?: 0.0   // ✅ chụp giá trị NGAY, trước khi reset

                    if (isCreatingCategory && newCategoryName.isNotBlank()) {
                        txVm.insertCategory(
                            Category(name = newCategoryName, icon = "ic_other", type = "expense")
                        ) { newId ->
                            budgetVm.insert(
                                Budget(
                                    categoryId  = newId.toInt(),
                                    limitAmount = enteredAmount,    // ✅ dùng giá trị đã chụp, không đọc lại state
                                    month       = budgetVm.selectedMonth,
                                    year        = budgetVm.selectedYear
                                )
                            )
                        }
                    } else if (selectedCategory != null) {
                        budgetVm.insert(
                            Budget(
                                categoryId  = selectedCategory!!,
                                limitAmount = enteredAmount,        // ✅ đồng bộ luôn cho nhánh này
                                month       = budgetVm.selectedMonth,
                                year        = budgetVm.selectedYear
                            )
                        )
                    }

                    // Reset state dialog — an toàn vì enteredAmount đã được chụp riêng ở trên
                    isCreatingCategory = false
                    newCategoryName    = ""
                    selectedCategory   = null
                    amount             = ""
                    showDialog         = false
                }) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isCreatingCategory = false
                    newCategoryName    = ""
                    selectedCategory   = null
                    amount             = ""
                    showDialog         = false
                }) {
                    Text("Hủy")
                }
            }
        )
    }



}