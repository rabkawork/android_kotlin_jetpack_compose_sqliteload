/***
 * @author: Ahadian Akbar
 * @date : 28 Feb 2024
 */

package com.example.kotlinsqlite.dao

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /*fun getDatabase(context: Context, databasePath: String? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbFile = databasePath?.let { File(it) }
                val dbName = if (dbFile != null && dbFile.exists()) databasePath else "item_database.db"

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                ).build()

                INSTANCE = instance
                instance
            }
        }*/

        fun getDatabase(context: Context, databasePath: String? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Jika ada path file database yang diimport dan file tersebut ada,
                // gunakan file tersebut. Jika tidak, gunakan default "item_database.db"
                val dbFile = databasePath?.let { File(it) }
                val dbName = if (dbFile != null && dbFile.exists()) {
                    Log.d("test","masuk ke " + dbFile.absolutePath)
                    // Catatan: Pastikan file berada di lokasi yang diakses oleh Room,
                    // misalnya di context.getDatabasePath()
                    dbFile.absolutePath


                } else {

                    Log.d("test","masuk ke item_database.db")
                    "item_database.db"
                }

//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    dbName
//                ).build()
//

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "item_database.db"
                ).build()

                INSTANCE = instance
                instance
            }
        }


    }
}
