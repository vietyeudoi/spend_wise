package com.example.spendwise.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.spendwise.data.entity.Transaction
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ShareCompat


object ExportUtils {

    // ── Xây nội dung CSV chung cho cả 2 hàm dưới ──────────────────────────────
    private fun buildCsvContent(transactions: List<Transaction>): String {
        val header = "id,title,amount,type,categoryId,note,date\n"
        val rows = transactions.joinToString("\n") { t ->
            "${t.id},\"${t.title.replace("\"", "'")}\",${t.amount},${t.type},${t.categoryId ?: ""},\"${t.note.replace("\"", "'")}\",${t.date}"
        }
        return header + rows
    }

    // ══════════════════════════════════════════════════════════════════════
    // 1. CHIA SẺ qua ứng dụng khác (Gmail, Zalo, Drive...)
    // ══════════════════════════════════════════════════════════════════════
    fun shareCsv(context: Context, transactions: List<Transaction>) {
        try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "SpendWise_${System.currentTimeMillis()}.csv")
            file.writeText(buildCsvContent(transactions))

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = ShareCompat.IntentBuilder(context)
                .setType("text/csv")
                .setStream(uri)
                .intent
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            context.startActivity(Intent.createChooser(intent, "Chia sẻ báo cáo CSV"))
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khi chia sẻ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // 2. LƯU VÀO THƯ MỤC DOWNLOADS công khai — người dùng tự mở Files app thấy được
    // ══════════════════════════════════════════════════════════════════════
    fun saveToDevice(context: Context, transactions: List<Transaction>): Boolean {
        val fileName = "SpendWise_${System.currentTimeMillis()}.csv"
        val csvContent = buildCsvContent(transactions)

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ — bắt buộc dùng MediaStore (Scoped Storage)
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(csvContent.toByteArray())
                    }
                } ?: throw Exception("Không tạo được file trong Downloads")
            } else {
                // Android 9 trở xuống — ghi trực tiếp, cần quyền WRITE_EXTERNAL_STORAGE
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                file.writeText(csvContent)
            }

            Toast.makeText(context, "Đã lưu vào Downloads: $fileName", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khi lưu file: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
    fun exportToCsv(context: Context, transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            Toast.makeText(context, "Không có giao dịch để xuất!", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val file = File(context.cacheDir, "transactions.csv")
            val writer = FileWriter(file)
            writer.append("ID,Title,Amount,Type,CategoryId,Note,Date\n")
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            transactions.forEach { t ->
                val dateStr = sdf.format(Date(t.date))
                val title = t.title.replace(",", " ").replace("\n", " ")
                val note = t.note.replace(",", " ").replace("\n", " ")
                writer.append("${t.id},$title,${t.amount},${t.type},${t.categoryId ?: ""},$note,$dateStr\n")
            }
            writer.flush()
            writer.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Xuất CSV"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Lỗi xuất file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
