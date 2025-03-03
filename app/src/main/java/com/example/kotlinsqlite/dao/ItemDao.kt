package com.example.kotlinsqlite.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    // Query untuk mendapatkan semua item
    // Bisa mengembalikan List<Item> untuk penggunaan biasa
    @Query("SELECT * FROM items ORDER BY name ASC")
    suspend fun getAllItems(): List<Item>

    // Alternatif: gunakan Flow untuk reactive updates
    // Flow akan mengamati perubahan database dan memperbarui UI secara otomatis
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getItemsAsFlow(): Flow<List<Item>>

    // Query untuk mendapatkan item berdasarkan ID
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Int): Item?

    // Operasi untuk menambahkan item baru
    // Mengembalikan ID dari item yang disisipkan
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item): Long

    // Operasi untuk memperbarui item yang ada
    @Update
    suspend fun update(item: Item)

    // Operasi untuk menghapus item
    @Delete
    suspend fun delete(item: Item)

    // Query untuk mencari item berdasarkan nama
    @Query("SELECT * FROM items WHERE name LIKE :searchQuery")
    suspend fun searchItems(searchQuery: String): List<Item>

    // Query untuk menghapus semua item
    @Query("DELETE FROM items")
    suspend fun deleteAll()

    // Query untuk menghitung jumlah total item
    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int
}