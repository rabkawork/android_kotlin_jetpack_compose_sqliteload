package com.example.kotlinsqlite.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mahasiswa")
data class Mahasiswa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val nim: String
)