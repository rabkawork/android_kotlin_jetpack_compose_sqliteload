/***
 * @author: Ahadian Akbar
 * @date : 28 Feb 2024
 */

package com.example.kotlinsqlite.dao

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.IOException

// Entity
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Int
)

// DAO
@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)
}


// Export Database
fun exportDatabase(context: Context) {
    val sourceFile = File(context.filesDir, "uploaded_database.db")
    val destFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "exported_database.db")
    try {
        sourceFile.copyTo(destFile, overwrite = true)
        Toast.makeText(context, "Database exported to Downloads folder", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        Toast.makeText(context, "Export failed", Toast.LENGTH_LONG).show()
    }
}
