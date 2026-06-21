package com.example.spendwise.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.spendwise.data.database.AppDatabase

class SpendWiseProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.example.spendwise.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/transactions")
        private const val TRANSACTIONS = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "transactions", TRANSACTIONS)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val context = context ?: return null
        val db = AppDatabase.getInstance(context)
        return when (uriMatcher.match(uri)) {
            TRANSACTIONS -> {
                db.openHelper.readableDatabase.query(
                    selection?.let { "SELECT * FROM transactions WHERE $it" } ?: "SELECT * FROM transactions"
                )
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            TRANSACTIONS -> "vnd.android.cursor.dir/$AUTHORITY.transactions"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
