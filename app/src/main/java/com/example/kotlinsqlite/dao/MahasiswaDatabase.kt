package com.example.kotlinsqlite.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Mahasiswa::class], version = 1, exportSchema = false)
abstract class MahasiswaDatabase : RoomDatabase() {
    abstract fun mahasiswaDao(): MahasiswaDao

    companion object {
        @Volatile
        private var INSTANCE: MahasiswaDatabase? = null

        fun getDatabase(context: Context): MahasiswaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MahasiswaDatabase::class.java,
                    "mydatabase.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}