package com.example.kotlinsqlite.utils


import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun exportDatabase(context: Context, databaseName: String) {
        val dbFile = context.getDatabasePath(databaseName)
        val exportPath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "exported_$databaseName"
        )

        try {
            dbFile.copyTo(exportPath, overwrite = true)
            Toast.makeText(context, "Database berhasil diekspor ke: ${exportPath.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Gagal mengekspor database!", Toast.LENGTH_LONG).show()
        }
    }


    fun exportDatabaseReal(context: Context, databasePath: String?) {
        val dbPath = databasePath ?: context.getDatabasePath("item_database.db").absolutePath
        val sourceFile = File(dbPath)

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val destFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "exported_$timestamp.db"
        )
        Log.e("Database Export", sourceFile.absolutePath)

        try {
            if (sourceFile.exists()) {
                sourceFile.copyTo(destFile, overwrite = true)
                Toast.makeText(context, "Database berhasil diekspor ke folder Downloads", Toast.LENGTH_LONG).show()
                Log.d("Database Export", "Database berhasil diekspor ke ${destFile.absolutePath}")
            } else {
                Toast.makeText(context, "Database tidak ditemukan!", Toast.LENGTH_LONG).show()
                Log.e("Database Export", "Database tidak ditemukan di $dbPath")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor database", Toast.LENGTH_LONG).show()
            Log.e("Database Export", "Gagal mengekspor database: ${e.message}")
        }
    }


    fun importDatabase(context: Context, databasePath: String, newDatabasePath: String) {
        val newDbFile = File(newDatabasePath)

        try {
            FileInputStream(File(databasePath)).use { input ->
                FileOutputStream(newDbFile).use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(context, "Database berhasil diimpor!", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Gagal mengimpor database!", Toast.LENGTH_LONG).show()
        }
    }
}
