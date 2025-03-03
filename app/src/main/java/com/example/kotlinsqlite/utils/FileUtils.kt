package com.example.kotlinsqlite.utils


import android.content.Context
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

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
