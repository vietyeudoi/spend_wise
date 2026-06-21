package com.example.spendwise.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Hiển thị số nhập vào dạng có dấu chấm phân cách hàng nghìn (1.000.000)
 * trong khi giá trị lưu trong state vẫn là số thuần "1000000" để parse bằng toDoubleOrNull().
 *
 * Cách dùng:
 *   OutlinedTextField(
 *       value = amount,
 *       onValueChange = { amount = it.filter { c -> c.isDigit() } },   // chỉ giữ số
 *       visualTransformation = ThousandsSeparatorTransformation(),
 *       suffix = { Text("đ") }
 *   )
 */
class ThousandsSeparatorTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formatted = StringBuilder()
        var groupCount = 0
        for (i in digits.indices.reversed()) {
            formatted.insert(0, digits[i])
            groupCount++
            if (groupCount % 3 == 0 && i != 0) {
                formatted.insert(0, '.')
            }
        }

        // Xây bảng ánh xạ vị trí con trỏ giữa text gốc (số thuần) và text hiển thị (có dấu chấm)
        val origToTransformed = IntArray(digits.length + 1)
        val transformedToOrig = mutableListOf<Int>()

        var origIndex = 0
        for (j in formatted.indices) {
            if (formatted[j] != '.') {
                origToTransformed[origIndex] = j
                origIndex++
            }
            transformedToOrig.add(origIndex.coerceAtMost(digits.length))
        }
        origToTransformed[digits.length] = formatted.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safe = offset.coerceIn(0, digits.length)
                return origToTransformed[safe]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safe = offset.coerceIn(0, formatted.length)
                return if (safe == 0) 0 else transformedToOrig.getOrElse(safe - 1) { digits.length }
            }
        }

        return TransformedText(AnnotatedString(formatted.toString()), offsetMapping)
    }
}