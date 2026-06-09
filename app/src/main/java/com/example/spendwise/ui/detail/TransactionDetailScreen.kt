package com.example.spendwise.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.ui.home.*
import com.example.spendwise.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    vm: TransactionViewModel = viewModel()
) {

    val tx by vm.getById(transactionId)
        .observeAsState()

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                ),
                title = {
                    Text(
                        text = "Chi tiết giao dịch",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    tx?.let { currentTx ->
                        IconButton(
                            onClick = {
                                vm.delete(currentTx)
                                onBack()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = ExpenseRed
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (tx == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Không tìm thấy giao dịch")
            }

        } else {

            val currentTx = tx!!

            val isIncome =
                currentTx.type.equals(
                    "income",
                    ignoreCase = true
                )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceWhite
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = currentTx.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                if (isIncome)
                                    "+${formatMoney(currentTx.amount)}"
                                else
                                    "-${formatMoney(currentTx.amount)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color =
                                if (isIncome)
                                    IncomeGreen
                                else
                                    ExpenseRed
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Loại: ${currentTx.type}"
                        )

                        Text(
                            text = "Danh mục ID: ${currentTx.categoryId}"
                        )

                        Text(
                            text = "Ngày: ${currentTx.date}"
                        )

                        Text(
                            text = "Ghi chú: ${
                                currentTx.note.ifBlank {
                                    "Không có"
                                }
                            }"
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        onEdit(currentTx.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue
                    )
                ) {

                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        "Chỉnh sửa giao dịch",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}