package com.example.spendwise.ui.budget

// --- PHẦN 1: IMPORT CÁC THƯ VIỆN GIAO DIỆN VÀ LOGIC ---
import androidx.compose.foundation.clickable // Giúp một thành phần (như Card) có thể nhấn vào được
import androidx.compose.foundation.layout.* // Chứa các thành phần bố cục: Box, Column, Row, Spacer...
import androidx.compose.foundation.lazy.* // Danh sách cuộn hiệu năng cao (chỉ vẽ những gì hiện trên màn hình)
import androidx.compose.foundation.shape.CircleShape // Tạo hình dạng vòng tròn (cho nút bấm)
import androidx.compose.material.icons.Icons // Bộ biểu tượng hệ thống
import androidx.compose.material.icons.filled.Add // Biểu tượng dấu cộng (+)
import androidx.compose.material3.* // Các thành phần giao diện chuẩn Material Design 3 (Card, Text, Button...)
import androidx.compose.material3.DropdownMenu // Menu thả xuống để chọn lựa
import androidx.compose.material3.DropdownMenuItem // Từng mục bên trong menu thả xuống
import androidx.compose.material3.OutlinedButton // Nút bấm có đường viền thanh mảnh
import androidx.compose.foundation.text.KeyboardOptions // Cấu hình kiểu bàn phím khi nhập liệu
import androidx.compose.ui.text.input.KeyboardType // Định nghĩa kiểu bàn phím: Số, Chữ, Email...
import androidx.compose.runtime.* // Thư viện cốt lõi để quản lý Trạng thái (State) trong Compose
import androidx.compose.runtime.livedata.observeAsState // Chuyển đổi dữ liệu từ Room (LiveData) sang State để cập nhật UI
import androidx.compose.ui.Alignment // Căn chỉnh các thành phần: giữa, trái, phải, trên, dưới
import androidx.compose.ui.Modifier // Công cụ mạnh mẽ nhất để chỉnh sửa kích thước, màu sắc, lề (padding)...
import androidx.compose.ui.text.font.FontWeight // Cấu hình độ đậm nhạt của chữ (Bold, Normal...)
import androidx.compose.ui.unit.dp // Đơn vị đo khoảng cách chuẩn Android (Density-independent Pixels)
import androidx.compose.ui.unit.sp // Đơn vị đo kích cỡ chữ chuẩn Android (Scale-independent Pixels)
import androidx.lifecycle.viewmodel.compose.viewModel // Hàm để khởi tạo và quản lý ViewModel trong Compose
import androidx.navigation.NavController // Biến dùng để điều hướng giữa các màn hình
import com.example.spendwise.data.entity.Budget // Thực thể (Table) Ngân sách trong cơ sở dữ liệu
import com.example.spendwise.ui.home.BrandBlue // Màu xanh chủ đạo của ứng dụng
import com.example.spendwise.ui.home.SurfaceWhite // Màu trắng nền bề mặt
import com.example.spendwise.ui.home.formatMoney // Hàm tự viết để biến 1000 thành "1.000 đ"
import com.example.spendwise.utils.* // Các hàm tiện ích hỗ trợ tính toán khác
import com.example.spendwise.viewmodel.BudgetViewModel // ViewModel xử lý các nghiệp vụ về Ngân sách
import com.example.spendwise.viewmodel.TransactionViewModel // ViewModel xử lý các nghiệp vụ về Giao dịch
import androidx.compose.material.icons.filled.Delete // Biểu tượng thùng rác dùng để xóa
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.spendwise.data.entity.Category // Thực thể (Table) Danh mục chi tiêu
import com.example.spendwise.utils.ThousandsSeparatorTransformation // Tự động thêm dấu chấm khi người dùng gõ tiền

/**
 * MÀN HÌNH QUẢN LÝ NGÂN SÁCH (BUDGET SCREEN)
 */
@Composable
fun BudgetScreen(
    navController: NavController, // Biến điều hướng màn hình
    budgetVm: BudgetViewModel = viewModel(), // Khởi tạo ViewModel Ngân sách
    txVm: TransactionViewModel = viewModel() // Khởi tạo ViewModel Giao dịch/Danh mục
){
    // --- PHẦN 2: LẤY DỮ LIỆU TỪ CƠ SỞ DỮ LIỆU (QUAN SÁT QUA LIVEDATA) ---

    // Lấy danh sách ngân sách của tháng đang chọn. Khi DB thay đổi, UI này tự vẽ lại.
    val budgets by budgetVm.budgets.observeAsState(emptyList())

    // Lấy toàn bộ danh mục để hiển thị tên danh mục (vì bảng Budget chỉ lưu ID danh mục)
    val categories by txVm.allCategories.observeAsState(emptyList())

    // Lấy thống kê số tiền đã tiêu thực tế theo từng danh mục trong tháng/năm hiện tại
    val spending by txVm.categorySpending(budgetVm.selectedMonth, budgetVm.selectedYear).observeAsState(emptyList())

    // --- PHẦN 3: CÁC BIẾN TRẠNG THÁI (UI STATE) ---

    // showDialog = true thì hiện bảng "Thêm ngân sách", false thì ẩn
    var showDialog by remember { mutableStateOf(false) }

    // Lưu chuỗi tiền người dùng gõ vào ô nhập (ví dụ: "500000")
    var amount by remember { mutableStateOf("") }

    // Lưu ID danh mục mà người dùng đang chọn để đặt hạn mức
    var selectedCategory by remember { mutableStateOf<Int?>(null) }

    // isCreatingCategory = true nếu người dùng muốn gõ tên danh mục mới thay vì chọn cái có sẵn
    var isCreatingCategory by remember { mutableStateOf(false) }

    // Lưu tên danh mục mới khi người dùng tự gõ
    var newCategoryName by remember { mutableStateOf("") }

    // dropdownExpanded = true thì menu danh sách danh mục sẽ thả xuống
    var dropdownExpanded by remember { mutableStateOf(false) }

    // editingBudget sẽ chứa đối tượng ngân sách nếu người dùng đang nhấn vào để sửa
    var editingBudget by remember { mutableStateOf<Budget?>(null) }

    // Số tiền mới khi đang sửa ngân sách
    var editAmount by remember { mutableStateOf("") }

    // showDeleteConfirm = true sẽ hiện hộp thoại "Bạn có chắc muốn xóa không?"
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // --- PHẦN 4: BỐ CỤC GIAO DIỆN CHÍNH ---
    Scaffold(
        // Cấu hình Nút  thêm
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, // Nhấn vào sẽ bật Dialog thêm mới
                containerColor = BrandBlue, // Đặt màu nền xanh
                contentColor = SurfaceWhite, // Đặt màu icon trắng
                shape = CircleShape, // Bo tròn nút thành hình tròn
                elevation = FloatingActionButtonDefaults.elevation(8.dp), // Hiệu ứng bóng đổ
                modifier = Modifier.padding(bottom = 16.dp) // Cách đáy 16dp
            ) {
                // Hình ảnh dấu cộng nằm giữa nút
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm ngân sách",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues -> //

        // Xếp các thành phần theo hàng dọc
        Column(
            modifier = Modifier
                .padding(paddingValues) // Tránh vùng an toàn hệ thống
                .padding(16.dp) // Thêm lề xung quanh nội dung 16dp
        ) {
            // --- HÀNG CHỌN THÁNG/NĂM ---
            Row(
                modifier = Modifier.fillMaxWidth(), // Rộng hết chiều ngang
                horizontalArrangement = Arrangement.SpaceBetween, // Nút sang 2 bên, chữ ở giữa
                verticalAlignment = Alignment.CenterVertically // Căn giữa các thành phần theo chiều dọc
            ) {
                // Nút bấm quay lại tháng trước
                Button(onClick = { budgetVm.prevMonth() }) {
                    Text("<")
                }

                // Hiển thị chữ Tháng/Năm (VD: 6/2026)
                Text(
                    text = "${budgetVm.selectedMonth}/${budgetVm.selectedYear}",
                    style = MaterialTheme.typography.titleLarge, // Font chữ lớn
                    fontWeight = FontWeight.Bold // Kiểu chữ đậm
                )

                // Nút bấm tiến đến tháng sau
                Button(onClick = { budgetVm.nextMonth() }) {
                    Text(">")
                }
            }

            // Khoảng trắng cao 16dp
            Spacer(modifier = Modifier.height(16.dp))

            // --- DANH SÁCH CÁC THẺ NGÂN SÁCH (LAZYCOLUMN) ---
            LazyColumn {
                // Duyệt qua từng bản ghi ngân sách trong danh sách
                items(budgets) { budget ->
                    // Tìm tên danh mục từ danh sách categories dựa trên ID
                    val name = categories.find { it.id == budget.categoryId }?.name ?: "N/A"

                    // Tìm số tiền đã tiêu thực tế của danh mục này (mặc định 0.0 nếu chưa tiêu gì)
                    val spent = spending.find { it.categoryId == budget.categoryId }?.total ?: 0.0

                    // Kiểm tra: Nếu tiền tiêu > hạn mức thì báo vượt ngưỡng
                    val isOver = isOverBudget(spent, budget.limitAmount)

                    // Thay đổi màu nền: Đỏ nhạt nếu vượt ngưỡng, Xám nhạt nếu bình thường
                    val cardBgColor = if (isOver) OverContainerColor else NormalContainerColor
                    // Thay đổi màu chữ: Đỏ đậm nếu vượt ngưỡng, Xanh đậm nếu bình thường
                    val textColor = if (isOver) OverTextColor else NormalTextColor

                    // Tính toán % tiến độ (ví dụ tiêu 500k/1tr thì progress = 0.5)
                    val progress = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat().coerceIn(0f, 1f) else 0f

                    // Thẻ Card hiển thị thông tin
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBgColor), // Áp dụng màu nền
                        modifier = Modifier
                            .fillMaxWidth() // Rộng hết cỡ
                            .padding(vertical = 6.dp) // Cách các thẻ khác 6dp
                            .clickable {
                                // Khi nhấn vào thẻ, gán đối tượng vào editingBudget để mở bảng sửa
                                editingBudget = budget
                                editAmount = budget.limitAmount.toString()
                            }
                    ) {
                        // Nội dung bên trong Card
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Dòng: Tên danh mục và Trạng thái
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
                                // Nhãn văn bản báo trạng thái
                                Text(
                                    text = if (isOver) "Vượt ngưỡng!" else "Bình thường",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Dòng: Số tiền đã chi và Hạn mức đặt ra
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Đã chi: ${formatMoney(spent)}", // Gọi hàm định dạng tiền
                                    color = textColor.copy(alpha = 0.8f), // Chữ mờ hơn một chút
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Hạn mức: ${formatMoney(budget.limitAmount)}",
                                    color = textColor.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Thanh Progress trực quan (Chạy từ trái sang phải)
                            LinearProgressIndicator(
                                progress = { progress }, // Giá trị phần trăm
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = textColor, // Màu của phần đã tiêu
                                trackColor = textColor.copy(alpha = 0.2f), // Màu của phần còn lại
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round // Bo tròn đầu thanh
                            )
                        }
                    }
                }
            }
        }

        // --- PHẦN 5: CÁC HỘP THOẠI (DIALOGS) ---

        // A. Hộp thoại SỬA / XÓA (Chỉ hiện khi editingBudget khác null)
          editingBudget?.let { budget ->
            // Tìm tên danh mục đang sửa
            val categoryName = categories.find { it.id == budget.categoryId }?.name ?: ""

            AlertDialog(
                onDismissRequest = { editingBudget = null }, // Nhấn ra ngoài thì tắt
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chỉnh sửa: $categoryName")
                        // Nút  xóa
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                text = {
                    // Ô nhập số tiền hạn mức mới
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("Hạn mức mới (đ)") },
                        suffix = { Text("đ") },
                        visualTransformation = ThousandsSeparatorTransformation(), // Tự thêm dấu chấm phần nghìn
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Hiện bàn phím số
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { // nhấn nút lưu
                        val newAmount = editAmount.toDoubleOrNull()
                        if (newAmount != null && newAmount > 0) {
                            // Cập nhật ngân sách vào DB thông qua ViewModel
                            budgetVm.update(budget.copy(limitAmount = newAmount))
                            editingBudget = null // Đóng dialog
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

            // Dialog hỏi lại "Bạn có chắc chắn muốn xóa không?"
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Xóa ngân sách?") },
                    text = { Text("Bạn có chắc muốn xóa hạn mức chi tiêu cho \"$categoryName\" không?") },
                    confirmButton = {
                        TextButton(onClick = {
                            budgetVm.delete(budget) // Thực hiện xóa trong DB
                            showDeleteConfirm = false // Đóng dialog hỏi
                            editingBudget = null // Đóng dialog sửa
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

    // B. Hộp thoại THÊM NGÂN SÁCH MỚI
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, //nhấn ra vùng trắng bên ngoài là tắt dialog
            title = { Text("Thiết lập ngân sách") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Nếu người dùng chọn tự gõ danh mục mới
                    if (isCreatingCategory) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it }, //cập nhập tên danh mục
                            label = { Text("Tên danh mục mới") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Nếu chọn từ danh sách danh mục có sẵn
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },// clickvaof thì mở danh mục thả xuống
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Hiển thị tên danh mục đã chọn, nếu chưa chọn thì hiện "Chọn danh mục"
                                Text(categories.find { it.id == selectedCategory }?.name ?: "Chọn danh mục")
                            }
                            // Danh sách thả xuống
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                // Chỉ lọc những danh mục thuộc loại Chi tiêu (expense)
                                categories.filter { it.type == "expense" }.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategory = category.id // Lưu ID danh mục được chọn
                                            dropdownExpanded = false // Đóng menu thả xuống
                                        }
                                    )
                                }
                                // Vạch kẻ ngang
                                HorizontalDivider()
                                // Mục chọn để tự tạo danh mục mới
                                DropdownMenuItem(
                                    text = { Text("+ Thêm danh mục mới") },
                                    onClick = {
                                        isCreatingCategory = true // Bật ô nhập tên danh mục
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Ô nhập số tiền hạn mức muốn đặt
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it }, //cập nhập số tiền gõ
                        label = { Text("Số tiền hạn mức") },
                        suffix = { Text("đ") },
                        visualTransformation = ThousandsSeparatorTransformation(), //tự thêm dấu chấm
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                // Khi nhấn Lưu
                TextButton(onClick = {
                    // chuyển số tiền từ chữ sang số  nếu lỗi mặc định
                    val enteredAmount = amount.toDoubleOrNull() ?: 0.0


                    // Trường hợp 1: Tạo mới hoàn toàn (Danh mục mới + Ngân sách mới)
                    if (isCreatingCategory && newCategoryName.isNotBlank()) {
                        // Gọi hàm chèn danh mục mới vào DB trước
                        txVm.insertCategory(
                            Category(name = newCategoryName, icon = "ic_other", type = "expense")
                        ) { newId ->
                            // Sau khi tạo xong danh mục, lấy ID đó để tạo tiếp Ngân sách
                            budgetVm.insert(
                                Budget(
                                    categoryId  = newId.toInt(), //dùng id vừa sinh ra
                                    limitAmount = enteredAmount,
                                    month       = budgetVm.selectedMonth,
                                    year        = budgetVm.selectedYear
                                )
                            )
                        }
                    }
                    // Trường hợp 2: Chọn danh mục có sẵn và tạo ngân sách
                    else if (selectedCategory != null) {
                        // Chèn thẳng bản ghi Ngân sách với ID danh mục đã biết
                        budgetVm.insert(
                            Budget(
                                categoryId  = selectedCategory!!,
                                limitAmount = enteredAmount,
                                month       = budgetVm.selectedMonth,
                                year        = budgetVm.selectedYear
                            )
                        )
                    }

                    // Reset toàn bộ biến trạng thái về mặc định sau khi xong việc
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
                // Nhấn Hủy thì chỉ reset và đóng, không lưu gì cả
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