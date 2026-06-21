package com.example.spendwise.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.ui.home.*
import com.example.spendwise.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    vm: TransactionViewModel = viewModel()
) {
    val tx by vm.getById(transactionId).observeAsState()
    val categories by vm.allCategories.observeAsState(emptyList())

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite),
                title = {
                    Text(
                        text = "Chi tiết giao dịch",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    tx?.let { currentTx ->
                        IconButton(onClick = {
                            vm.delete(currentTx)
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = ExpenseRed)
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (tx == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Không tìm thấy giao dịch", color = Color.Gray)
            }
            return@Scaffold
        }

        val currentTx = tx!!
        val isIncome = currentTx.type.equals("income", ignoreCase = true)
        val accentColor = if (isIncome) IncomeGreen else ExpenseRed

        val categoryName = categories.find { it.id == currentTx.categoryId }?.name ?: "Không có danh mục"

        val dateFormatted = remember(currentTx.date) {
            SimpleDateFormat("EEEE, dd/MM/yyyy 'lúc' HH:mm", Locale("vi", "VN"))
                .format(Date(currentTx.date))
                .replaceFirstChar { it.uppercase() }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Thẻ số tiền chính ────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon tròn thể hiện loại giao dịch
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(accentColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentTx.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isIncome) "+${formatMoney(currentTx.amount)}" else "-${formatMoney(currentTx.amount)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = accentColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (isIncome) "Thu nhập" else "Chi tiêu",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Thẻ thông tin chi tiết ────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

                    DetailRow(
                        icon = Icons.Default.Category,
                        label = "Danh mục",
                        value = categoryName,
                        iconTint = BrandBlue
                    )

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Thời gian",
                        value = dateFormatted,
                        iconTint = BrandBlue
                    )

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    DetailRow(
                        icon = Icons.Default.Notes,
                        label = "Ghi chú",
                        value = currentTx.note.ifBlank { "Không có" },
                        iconTint = BrandBlue
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Nút chỉnh sửa ─────────────────────────────────────────────────────
            Button(
                onClick = { onEdit(currentTx.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chỉnh sửa giao dịch", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconTint.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}