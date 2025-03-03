package com.example.kotlinsqlite.dao

import androidx.room.*

@Dao
interface MahasiswaDao {
    @Query("SELECT * FROM mahasiswa")
    suspend fun getAll(): List<Mahasiswa>

    @Insert
    suspend fun insert(mahasiswa: Mahasiswa)

    @Delete
    suspend fun delete(mahasiswa: Mahasiswa)
}