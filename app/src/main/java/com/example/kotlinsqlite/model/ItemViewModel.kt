/***
 * @author: Ahadian Akbar
 * @date : 28 Feb 2024
 */
package com.example.kotlinsqlite.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinsqlite.dao.Item
import com.example.kotlinsqlite.dao.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {

    val items: StateFlow<List<Item>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addItem(name: String, quantity: Int) {
        viewModelScope.launch {
            repository.addItem(name, quantity)
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}


