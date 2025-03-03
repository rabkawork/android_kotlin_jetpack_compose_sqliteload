/***
 * @author: Ahadian Akbar
 * @date : 28 Feb 2024
 */
package com.example.kotlinsqlite.model

import com.example.kotlinsqlite.dao.Item



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinsqlite.dao.ItemDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ItemDatabase.getDatabase(application)
    private val itemDao = database.itemDao()

    // Menggunakan StateFlow untuk menyimpan dan mengembalikan daftar item
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems.asStateFlow()

    // Memuat semua item dari database
    fun loadItems() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val items = itemDao.getAllItems()
                _allItems.value = items
            }
        }
    }

    // Menambahkan item baru ke database
    suspend fun addItem(name: String, quantity: Int) {
        withContext(Dispatchers.IO) {
            val item = Item(name = name, quantity = quantity)
            itemDao.insert(item)
            // Memperbarui daftar item setelah penambahan
            _allItems.value = itemDao.getAllItems()
        }
    }

    // Memperbarui item yang ada di database
    suspend fun updateItem(item: Item) {
        withContext(Dispatchers.IO) {
            itemDao.update(item)
            // Memperbarui daftar item setelah pembaruan
            _allItems.value = itemDao.getAllItems()
        }
    }

    // Menghapus item dari database
    suspend fun deleteItem(item: Item) {
        withContext(Dispatchers.IO) {
            itemDao.delete(item)
            // Memperbarui daftar item setelah penghapusan
            _allItems.value = itemDao.getAllItems()
        }
    }

    // Fungsi untuk mendapatkan item berdasarkan ID
    suspend fun getItemById(id: Int): Item? {
        return withContext(Dispatchers.IO) {
            itemDao.getItemById(id)
        }
    }

    // Fungsi ini jangan digunakan dari UI thread - untuk memastikan kode sebelumnya masih berfungsi
    fun getAllItems(): List<Item> {
        return _allItems.value
    }
}


