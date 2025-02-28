/***
 * @author: Ahadian Akbar
 * @date : 28 Feb 2024
 */


package com.example.kotlinsqlite.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlinsqlite.dao.AppDatabase
import com.example.kotlinsqlite.dao.ItemRepository

class ItemViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val repository: ItemRepository

    init {
        val database = AppDatabase.getDatabase(context)
        repository = ItemRepository(database.itemDao())
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}